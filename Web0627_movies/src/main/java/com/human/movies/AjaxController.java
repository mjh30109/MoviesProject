package com.human.movies;

import java.util.Locale;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.human.service.IF_M_WishListService;
import com.human.service.IF_ServiceCenterService;
import com.human.service.IF_UserService;

// 비동기 처리 역할을 맡은 컨트롤러 
@Controller
public class AjaxController {

	@Inject
	IF_UserService usvs;
	@Inject
	IF_ServiceCenterService scsvs;
	@Inject
	IF_M_WishListService wsvs;

	// 회원가입시에 DB에 저장돼있는 id와 회원가입시 입력한 아이디와 일치하는지 판단
	// 일치하면 사용유저 있는걸로 판단 후 JSP 파일에서 결과를 정의
	@RequestMapping(value = "/idCheck", method = RequestMethod.POST)
	@ResponseBody
	public int u_idCheck(@RequestParam("u_id") String u_id) throws Exception {
		int result = usvs.idDuplicateCheck(u_id);
		return result;
	}

	// 고객센터에서 등록돼있는 글제목을 클릭하면 조회수가 올라갈 수 있도록 하는 비동기처리.
	@RequestMapping(value = "/cntplus", method = RequestMethod.POST)
	@ResponseBody
	public void cntplus(Locale locale, Model model, @RequestParam("sc_num") int sc_num) throws Exception {
		System.out.println("sc_num : " + sc_num);
		scsvs.cntplus(sc_num);
	}

	// 로그인한 회원이 영화 자세히보기에서 찜등록을 누르면 페이지 리로드 없이 등록을 할 수 있는
	// 기능을 담당한 메서드 회원은 5개까지 찜등록을 할 수 있다.
	@RequestMapping(value = "/m_wishList_insert", method = RequestMethod.POST)
	@ResponseBody
	public int m_wishList_insert(Locale locale, Model model, @RequestParam("u_id") String u_id,
			@RequestParam("m_name") String m_name) throws Exception {
		int wishListSize = wsvs.myWishList(u_id).size();
		int result = wsvs.countWishList(u_id, m_name);
		if (wishListSize > 4) {
			return 2;
		} else if (result == 0) {
			// 5이하 일 때
			wsvs.insert(u_id, m_name);
			return 1;
		} else {
			return 3;
		}
	}

}
