package com.aram.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.aram.dao.UserDAO;
import com.aram.dto.UserDTO;
import com.aram.utils.EncryptionUtils;
import com.google.gson.Gson;

@WebServlet("*.user")
public class UserController extends HttpServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAction(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAction(request, response);
	}
	protected void doAction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uri = request.getRequestURI();
		System.out.println("요청 uri : " + uri);
    
		request.setCharacterEncoding("utf-8");
		
		
		if(uri.equals("/idCheck.user")) { // 아이디 중복체크 요청
			String id = request.getParameter("id");
			System.out.println("아이디 중복확인 : " + id);
			UserDAO dao = new UserDAO();
			
			try {
				int rs = dao.checkId(id);
				if(rs == 1) { // 사용할 수 있는 아이디라면
					System.out.println("아이디 사용가능");
					response.getWriter().append("ok");
				}else if(rs == 0) { // 중복된 아이디라면
					System.out.println("아이디 사용불가능");
					response.getWriter().append("nope");
				}	
			}catch(Exception e) {
				e.printStackTrace();
			}

		}else if(uri.equals("/join.user")){// 회원가입 페이지 요청
			response.sendRedirect("/member/join.jsp");
			
		}else if(uri.equals("/signup.user")) { // 회원가입 하기
			String name = request.getParameter("name");
			String id = request.getParameter("id");
			String nickname = request.getParameter("nickname");
			String password = request.getParameter("password");
			String phone = request.getParameter("phone");
			String email = request.getParameter("email");
			String postcode = request.getParameter("postcode");
			String roadAddr = request.getParameter("roadAddr");
			String detailAddr = request.getParameter("detailAddr");
			
			System.out.println(name + " : " + id + " : " + nickname + " : " + password
					+ " : " + phone + " : " + email + " : " + roadAddr + " : " + detailAddr);
			
			UserDAO dao = new UserDAO();
			try {
				password = EncryptionUtils.getSHA512(password);
				System.out.println("암호화된 데이터 : " + password);
				
				int rs = dao.signup(new UserDTO(id, password, name, nickname, phone, email, postcode, roadAddr, detailAddr, null, "n", null, "n"));
				if(rs > 0) {
					response.sendRedirect("/login.user"); 
					//response.sendRedirect("/member/emailSendAction.jsp");
					//이메일 인증때문에..잠시
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}else if(uri.equals("/login.user")){ // 로그인창 요청
			response.sendRedirect("/member/login.jsp");
		}else if(uri.equals("/loginProc.user")) { // 로그인 요청
			String id = request.getParameter("id");
			String pw = request.getParameter("pw");
			System.out.println(id + " : " + pw);
			
			UserDAO dao = new UserDAO();
			try {
				pw = EncryptionUtils.getSHA512(pw);
				System.out.println("암호회된 비번 : " + pw);
				UserDTO dto = dao.isLoginOk(id, pw);
				if(dto != null) {
					System.out.println("로그인 성공");
					request.setAttribute("rs", true);
					HttpSession session = request.getSession();
					session.setAttribute("loginSession", dto);

				}else {
					System.out.println("로그인 실패");
					request.setAttribute("rs", false);
					
				}
				request.getRequestDispatcher("/main").forward(request, response);
				//request.getRequestDispatcher("/member/emailSendAction.jsp").forward(request, response);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}else if(uri.equals("/kakaoLogin.user")) { // 카카오 로그인
			String kakaoid = request.getParameter("userid");
			System.out.println("카카오 아이디 : " + kakaoid);
			
			UserDAO dao = new UserDAO();
			try {
				UserDTO dto = dao.kakaoLogin(kakaoid);
				
				if(dto != null) {
					System.out.println("로그인 성공");
					
					response.getWriter().append("ok");
					HttpSession session = request.getSession();
					session.setAttribute("loginSession", dto);
					
				}else {
					System.out.println("로그인 실패");
					response.getWriter().append("fail");
				}
				
			}catch(Exception e) {
				e.printStackTrace();
			}
		}else if(uri.equals("/kakaoSignup.user")) {//카카오 회원가입
			String kakaoid = request.getParameter("userid");
			String name = request.getParameter("name");
			String email = request.getParameter("email");
			System.out.println("카카오 아이디 : " + kakaoid + " 카카오 이름 : " +name+ " 카카오 이메일 : " +email);
			request.setAttribute("kakaoid", kakaoid);
			request.setAttribute("kakaoname", name);
			request.getRequestDispatcher("/member/kakaojoin.jsp").forward(request, response);
			
		}else if(uri.equals("/kakaoSignupProc.user")) { // 카카오 회원가입 요청
			String name = request.getParameter("name");
			String id = request.getParameter("id");
			String nickname = request.getParameter("nickname");
			String phone = request.getParameter("phone");
			String email = request.getParameter("email");
			String postcode = request.getParameter("postcode");
			String roadAddr = request.getParameter("roadAddr");
			String detailAddr = request.getParameter("detailAddr");
			
			System.out.println(name + " : " + id + " : " + nickname + " : " + phone + " : " + email + " : " + roadAddr + " : " + detailAddr);
			
			UserDAO dao = new UserDAO();
			try {
				String pw_id = EncryptionUtils.getSHA512(id);
				System.out.println("암호회된 비번 : " + pw_id);
				int rs = dao.signup(new UserDTO(id, pw_id, name, nickname, phone, email, postcode, roadAddr, detailAddr, id, "n", null, "n"));
				if(rs > 0) {
					response.sendRedirect("/login.user");
				}
			}catch(Exception e) {
				e.printStackTrace();
			}

		}else if(uri.equals("/toFinduser.user")) { // 아이디/비밀번호 찾기 페이지로 이동 요청
			response.sendRedirect("/member/finduser.jsp");			
		}else if(uri.equals("/searchToForgotId.user")) { // 아이디 찾기 요청
			String name = request.getParameter("name");
			String email = request.getParameter("email");
			
			System.out.println(name + " : " + email);
			UserDAO userDao = new UserDAO();
			try {
				String id = userDao.findId(name, email);
				
				if(id == null) { // 해당 정보의 아이디가 존재하지 않음
					System.out.println("해당 정보의 아이디가 존재하지 않음");
					request.setAttribute("rsFindId", "n");
				} else { // 아이디 찾기 성공
					System.out.println("아이디 찾기 성공 : " + id);
					request.setAttribute("rsFindId", id);
				}
				request.getRequestDispatcher("/member/finduser.jsp").forward(request, response);
				
			} catch(Exception e) {
				e.printStackTrace();
			}
		} else if (uri.equals("/searchToForgotPw.user")) { // 비밀번호찾기
			String id = request.getParameter("id");
			String email = request.getParameter("email");
			
			System.out.println(id + " : " + email);
			
			UserDAO userDao = new UserDAO();
			try {
				String rsId = userDao.toChangePw(id, email);
				System.out.println("rsId : " + rsId);
				
				if(rsId == null) {
					request.setAttribute("rsChangePw", "n");
				} else {
					request.setAttribute("rsChangePw", "y");
					request.setAttribute("rsChangePwIdValue", rsId);
				}
				request.getRequestDispatcher("/member/finduser.jsp").forward(request, response);
				
			} catch(Exception e) {
				e.printStackTrace();
			}
			
		} else if (uri.equals("/toChange_pw.user")) { // 분실시 비밀변호 변경 페이지 요청
			String rsId = request.getParameter("rsId");
			System.out.println("비밀번호 찾기에서 가져온 id : " + rsId);
			request.setAttribute("idFromFindPw", rsId);
			request.getRequestDispatcher("/member/change_pw.jsp").forward(request, response);
			

		} else if (uri.equals("/completeChangePw.user")) { // 분실시 비밀번호 변경
			String userId = request.getParameter("userId");
			String password = request.getParameter("password");
			System.out.println("아이디 : " + userId + "/ 변경된 비밀번호 : " + password);
			
			try {
				password = EncryptionUtils.getSHA512(password);
				System.out.println("암호화된 데이터 : " + password);
				UserDAO userDao = new UserDAO();
				int rs = userDao.changePw(userId, password);
				if(rs>0) { // 변경 성공
					request.setAttribute("rsChangePw", "y");
				} else { // 변경 실패
					request.setAttribute("rsChangePw", "n");
				}
				request.getRequestDispatcher("/member/change_pw.jsp").forward(request, response);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		
		
		
	}
}
