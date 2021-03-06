package com.careye.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.careye.common.domain.EntityTreeUtil;
import com.careye.common.domain.MenuTree;
import com.careye.common.service.UserLoginService;
import com.careye.component.springhelper.BeanLocator;
import com.careye.constant.WebConstants;
import com.careye.system.domain.BlocUser;
import com.careye.system.service.AuthorityService;
import com.careye.utils.SessionUtils;

public class MenuTreeServlet extends HttpServlet {
	
	private AuthorityService authorityService;
	private UserLoginService userLoginService;
	private List<MenuTree> menuList;
	private String usergroupid;
	private String menutree;
	
	private Map<String, String> paramsMap; // 用于接收参数
	
	/**
	 * Constructor of the object.
	 */
	public MenuTreeServlet() {
		super();
	}
 
	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doPost(request, response);
	}

	@SuppressWarnings("unchecked")
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		response.reset();// 清空输出流   
		response.setContentType("text/html");
		response.setCharacterEncoding("utf-8");   
		
		try{
			
			HttpSession session = request.getSession(true);
			BlocUser user = (BlocUser)session.getAttribute(WebConstants.LOGIN_USER);
//			User user = SessionUtils.getUser();
			if(user == null){
				return;
			}
			
			authorityService = (AuthorityService)BeanLocator.getInstance().getBean("authorityService");
			userLoginService = (UserLoginService)BeanLocator.getInstance().getBean("userLoginService");
			
			String usergroupid = request.getParameter("blocgroupid");
			List menuidList = Collections.emptyList();
			if(usergroupid != null && !usergroupid.equals("")){		
				if(usergroupid.equals("-1")){
					menuidList = authorityService.getAuthorityMenuId();
				}else{
					//找出用户组下面的权限菜单列表
					menuidList = authorityService.getUserGroupMenuId(Integer.parseInt(usergroupid));
				}
			}
			
			menuList = authorityService.allMenuList();
			for(MenuTree m :menuList){
				if(m.getLeaftype() == 0){
					m.setExpanded(true);
				}else if(m.getLeaftype() == 1){
					m.setExpanded(true);
					//m.setLeaf(true);
//					List childrenlist = authorityService.getChildrenMenuId(Integer.parseInt(m.getId()));
//					if(childrenlist.size() > 0){ //判断有没有子节点，有的话
//						m.setExpanded(true);
//					}else{ 	//如果没有
//						m.setLeaf(true);
//					}
				}else{
					m.setLeaf(true);
				}
				if(menuidList.contains(Integer.parseInt(m.getId()))){		//	用户组权限包含该菜单
					m.setChecked(true);
				}
				
			}
			menutree =  EntityTreeUtil.getTreeJsonString(menuList,1,0);
			
			//每次重新加载时刷新session信息
			BlocUser userInfo = userLoginService.getUserInfo(user);
			if(userInfo != null){
				session.setAttribute(WebConstants.LOGIN_USER, userInfo);
			}
				
			PrintWriter out = response.getWriter();
			out.println(menutree);
			out.flush();
			out.close();
			
		}catch(Exception e){
			PrintWriter out = response.getWriter();
			out.println("[{text: '系统加载错误，请重试。。。', id:1,expanded: true}]");
			out.flush();
			out.close();
			e.printStackTrace();
		}
	}

	public void init() throws ServletException {
		// Put your code here
	}

}
