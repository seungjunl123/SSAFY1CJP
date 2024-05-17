package com.cjp.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cjp.model.dto.User;
import com.cjp.model.service.UserService;
import com.cjp.util.JwtUtil;

@RestController
@RequestMapping("/user")
public class UserRestController {
	private static final String SUCCESS = "success";
	private static final String FAIL = "fail";

	@Autowired
	private JwtUtil jwtUtil;
	
	@Autowired
	private UserService userService;

	
	public UserRestController(UserService userService) {
		this.userService = userService;
	}

	// 로그인
	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(@RequestBody User user) {
		HttpStatus status = null;
		Map<String, Object> result = new HashMap<>();

		// 사용자 인증
		User checkUser = userService.login(user.getId(), user.getPassword());

		// login은 id, pw 일치 시 해당 유저 정보 반환, 일치하지 않을 시 null 반환
		// 사용자 인증에 성공했을 경우
		if (checkUser != null) {
			// 토큰 생성
			String token = jwtUtil.createToken(checkUser.getId());
			result.put("success!", SUCCESS);
			result.put("access-token", token);
			status = HttpStatus.OK;
		} else {
			result.put("fail!", FAIL);
			status = HttpStatus.UNAUTHORIZED;
		}

		return new ResponseEntity<>(result, status);
	}

	// 회원가입
	@PostMapping("/signup")
	public ResponseEntity<?> signup(@RequestBody User user) {
		User tmp = userService.search(user.getId());
		if (tmp == null) {
			userService.signup(user);
			return new ResponseEntity<>("Signup successful", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("이미 있는 아이디입니다", HttpStatus.CONFLICT);
		}
	}

	// 유저 정보 조회
	@GetMapping("/{id}")
	public ResponseEntity<User> userInfo(@PathVariable("id") String id) {
		System.out.println("들어왔어!");
		User user = userService.search(id);
		System.out.println(user);
		return new ResponseEntity<User>(user, HttpStatus.OK);

	}

	// 성공적으로 회원가입이 되었어!
	// 1. 회원가입 축하합니다. 페이지로 이동
	// 2. 로그인 페이지로 이동(v)
	// 3. 게시글 목록 화면으로 이동
	// 3-1. 지금 User 객체를 그대로 실어서 로그인 요청을 보내기
	// 3-2. 세션불러다가 직접 등록해버리고 넘어가

	/*
	 * 이전 꺼!!!!! private final UserService userService;
	 * 
	 * @Autowired public UserController(UserService userService) { this.userService
	 * = userService; }
	 * 
	 * //로그인 페이지 주세요
	 * 
	 * @GetMapping("/login") public String loginForm() { return "/user/loginform"; }
	 * 
	 * //로그인 해주세요
	 * 
	 * @PostMapping("/login") // public String login(@RequestParam("id") String
	 * id, @RequestParam("password") String password) { // // } public String
	 * login(@ModelAttribute User user, HttpSession session) { User tmp =
	 * userService.login(user.getId(), user.getPassword()); //tmp에 들어갈 수 있는 값은? //1.
	 * 실제로 로그인 잘 되었다면 유저객체가 반환이 되어 사용할 수 있음 //2. 뭔가 아이디나 비밀번호가 틀렸어! null if(tmp ==
	 * null) return "redirect:login";
	 * 
	 * //로그인 성공 (세션 영역에 정보를 저장했다) session.setAttribute("loginUser", tmp.getName());
	 * return "redirect:list"; }
	 * 
	 * //로그아웃
	 * 
	 * @GetMapping("/logout") public String logout(HttpSession session) { //
	 * session.removeAttribute("loginUser"); session.invalidate(); //리스트 화면으로 갔는데,
	 * 로그인페이지로 보낼 수도 있다. return "redirect:list"; }
	 * 
	 * 
	 * 
	 * @GetMapping("/users") public String userList(Model model) {
	 * model.addAttribute("userList", userService.getUserList()); return
	 * "/user/adminPage"; }
	 */

}
