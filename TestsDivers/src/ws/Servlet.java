package ws;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class Servlet extends HttpServlet {
	
	private ServletConfig servletConfig;
	private ServletContext ctx;
	private String contextPath;
	
	@Override 
	public void init(ServletConfig servletConfig) throws ServletException {
		this.servletConfig = servletConfig;
		this.ctx = this.servletConfig.getServletContext();
		this.contextPath = ctx.getContextPath().substring(1);
	}
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String uri = req.getRequestURI().substring(contextPath.length() + 1);
		String qs = req.getQueryString();
		String a = req.getParameter("a");
		if (a != null) a = a.replaceAll(" ", "+");
		String b = req.getParameter("b");
		// System.out.println(uri);
		resp.getWriter().print("[" + contextPath + "][" + uri + "][" + a + "][" + b + "][" + new Date() + "]");
	}
}
