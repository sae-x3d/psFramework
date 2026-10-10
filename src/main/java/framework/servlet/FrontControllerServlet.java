package framework.servlet;

import framework.annotations.Controller;
import framework.annotations.WebApi;
import framework.listener.AppListener;
import framework.models.ModelAndView;
import framework.models.UrlMethod;
import framework.util.ToJson;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrontControllerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String PREFIX_INIT_PARAM = "prefix";
    private static final String SUFFIX_INIT_PARAM = "suffix";
    private static final String DEFAULT_PREFIX = "/WEB-INF/views/";
    private static final String DEFAULT_SUFFIX = ".jsp";

    private Map<String, Map<UrlMethod, Method>> urlMappingMap;
    private List<Class<?>> controllerClasses;
    private Map<Class<?>, Object> controllerInstances = new HashMap<>();
    private String prefix;
    private String suffix;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext ctx = config.getServletContext();

        this.controllerClasses = (List<Class<?>>) ctx.getAttribute(AppListener.CONTROLLER_CLASSES_ATTR);
        this.urlMappingMap = (Map<String, Map<UrlMethod, Method>>) ctx.getAttribute(AppListener.URL_MAPPING_MAP_ATTR);

        if (this.controllerClasses != null) {
            for (Class<?> clazz : this.controllerClasses) {
                try {
                    this.controllerInstances.put(clazz, clazz.getDeclaredConstructor().newInstance());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        this.prefix = config.getInitParameter(PREFIX_INIT_PARAM);
        if (this.prefix == null) this.prefix = DEFAULT_PREFIX;

        this.suffix = config.getInitParameter(SUFFIX_INIT_PARAM);
        if (this.suffix == null) this.suffix = DEFAULT_SUFFIX;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (controllerClasses == null || urlMappingMap == null) {
            resp.setContentType("text/html");
            resp.getWriter().println("<p>Framework non initialise (AppListener absent ?)</p>");
            return;
        }

        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String pathInfo = requestURI.substring(contextPath.length());
        if (pathInfo.startsWith("/")) {
            pathInfo = pathInfo.substring(1);
        }

        UrlMethod key = new UrlMethod(pathInfo, req.getMethod());
        Method method = null;
        String controllerName = null;
        for (Map.Entry<String, Map<UrlMethod, Method>> entry : urlMappingMap.entrySet()) {
            if (entry.getValue().containsKey(key)) {
                method = entry.getValue().get(key);
                controllerName = entry.getKey();
                break;
            }
        }

        if (method == null) {
            resp.setContentType("text/html");
            PrintWriter out = resp.getWriter();
            out.println("<html><body>");
            out.println("<h3>Pas de mapping trouve pour " + req.getMethod() + " " + pathInfo + "</h3>");
            out.println("<p>Liste des mappings disponibles:</p>");
            out.println("<ul>");
            for (Map.Entry<String, Map<UrlMethod, Method>> entry : urlMappingMap.entrySet()) {
                for (Map.Entry<UrlMethod, Method> mEntry : entry.getValue().entrySet()) {
                    out.println("<li>" + entry.getKey() + " -> " + mEntry.getKey()
                            + " (method: " + mEntry.getValue().getName() + ")</li>");
                }
            }
            out.println("</ul>");
            out.println("</body></html>");
            return;
        }

        Object result;
        try {
            Object controllerInstance = controllerInstances.get(method.getDeclaringClass());
            result = method.invoke(controllerInstance);
        } catch (Exception e) {
            resp.setContentType("text/html");
            PrintWriter out = resp.getWriter();
            out.println("<p>Invok KO: " + e.getMessage() + "</p>");
            e.printStackTrace(out);
            return;
        }

        WebApi api = method.getAnnotation(WebApi.class);
        if (api != null) {
            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.write(api.toJson() ? ToJson.toJson(result) : String.valueOf(result));
            return;
        }

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<html><body>");

        out.println("<h2>Liste des controllers</h2>");
        out.println("<ul>");
        for (Class<?> clazz : controllerClasses) {
            Controller ann = clazz.getAnnotation(Controller.class);
            out.println("<li>" + clazz.getSimpleName() + " → @Controller(\"" + ann.value() + "\")</li>");
        }
        out.println("</ul>");

        if (result instanceof ModelAndView) {
            ModelAndView mv = (ModelAndView) result;
            for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                req.setAttribute(entry.getKey(), entry.getValue());
            }
            req.getRequestDispatcher(prefix + mv.getView() + suffix).forward(req, resp);
            return;
        }

        out.println("<h3>Controller->Method: " + controllerName + "->" + method.getName() + "</h3>");
        out.println("<p>Result: " + result + "</p>");
        out.println("</body></html>");
    }
}
