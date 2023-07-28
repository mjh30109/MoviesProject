package com.human.movies;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.human.service.IF_M_CommentService;
import com.human.service.IF_MoviesService;
import com.human.service.IF_UserService;
import com.human.util.FileDataUtil;
import com.human.vo.MoviesVO;
import com.human.vo.PageVO;
import com.human.vo.m_commentVO;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

	/**
	 * Simply selects the home view to render by returning its name.
	 */
	// 홈

	@Inject
	FileDataUtil util;
	@Inject
	IF_MoviesService msvs;
	@Inject
	IF_UserService usvs;
	@Inject
	IF_M_CommentService mcsvs;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) throws Exception {
		// 홈 화면에 동적으로 나타낼 이미지를 불러올 장르별 영화의정보
		model.addAttribute("comedyview", msvs.comedyView());
		// 장르 : 코미디
		model.addAttribute("animationview", msvs.animationView());
		// 장르 : 애니메이션
		model.addAttribute("horrorview", msvs.horrorView());
		// 장르 : 공포
		model.addAttribute("actionview", msvs.actionView());
		// 장르 : 액션
		model.addAttribute("romanceview", msvs.romanceView());
		// 장르 : 로맨스
		return "home";
	}

	// 영화 등록을 할 수 있게 만들어줄 JSP로 return
	@RequestMapping(value = "/m_input", method = RequestMethod.GET)
	public String m_input(Locale locale, Model model) {
		return "m_input";
	}

	// 영화 등록을 한 후 form태그에서 지정한 action의 매핑 주소 m_input_save
	@RequestMapping(value = "/m_input_save", method = RequestMethod.POST)
	public String m_input_save(Locale locale, Model model, @ModelAttribute("") MoviesVO mvo,
			@ModelAttribute("") MultipartFile file) throws Exception {
		String newName = util.fileUpload(file);
		// 파일의 이름을 바꾸는 fileUpload 메서드 정의 후 사용
		mvo.setM_filename(newName);
		// form 태그에서 파일을 업로드 한뒤 파일의 이름을 재정의
		msvs.insertMovie(mvo);
		// 파일 이름을 재정의 한 후 DB Insert Mapper insert
		return "m_input";
	}

	// 홈화면에서 [영화] 버튼을 눌렀을 때 보여줄 화면제공.
	@RequestMapping(value = "/list_view", method = RequestMethod.GET)
	public String list_view(Locale locale, Model model, PageVO pvo) throws Exception {
		// 영화의 수가 10개 미만이라면 페이지는 0이 된다. 0이 됐을 때의 알고리즘은 오류가날 수 있으므로
		// pageVO의 페이지를 1로 설정해준다.
		if (pvo.getPage() == null) {
			pvo.setPage(1);
		} else {
		}
		pvo.setPerPageNum(6);
		// 데이터의 갯수를 pageVO 에게 넘겨준다.
		pvo.setTotalCount(msvs.listCount());
		// 데이터의 갯수를 받은 PageVO는 calcPage라는 알고리즘 메서드를 사용해
		// 페이지의 정보들을 설정
		pvo.calcPage();
		model.addAttribute("pageVO", pvo);
		model.addAttribute("movies", msvs.movieList(pvo));
		return "list_view";
	}

	// 영화검색_영화제목
	// a링크로 검색단어를 RequestParam으로 받아올 수 있다.
	// RequestParam으로 받은 m_word의 값을 Mapper에서 쿼리문 정의 후
	// 쿼리문에서 where 절에 m_word를 입력해 걸러낸 데이터를
	// list_view에게 전달
	@RequestMapping(value = "/m_search", method = RequestMethod.GET)
	public String search_m(Locale locale, Model model, @RequestParam("m_word") String Sword) throws Exception {
		if (msvs.searchMovie(Sword) != null) {
			model.addAttribute("smovie", msvs.searchMovie(Sword));
		} else {
			model.addAttribute("falseList", "테스트용");
		}
		return "list_view";
	}

	// 영화삭제_영화제목
	@RequestMapping(value = "/m_delete", method = RequestMethod.GET)
	public String delete_m(Locale locale, Model model) {
		// redirect로 URL을 지정해준다.
		return "redirect:list_view";
	}

	// 영화수정 폼
	@RequestMapping(value = "/m_mod", method = RequestMethod.GET)
	public String mod_m(Locale locale, Model model) {

		return "m_mod";
	}

	// 영화수정 _저장
	@RequestMapping(value = "/m_mod_save", method = RequestMethod.GET)
	public String mod_m_save(Locale locale, Model model) {

		return "redirect:list_view";
	}

	// 영화자세히보기
	@RequestMapping(value = "/m_detail", method = RequestMethod.GET)
	public String m_detail(Locale locale, Model model, @RequestParam("m_name") String m_name, HttpSession session,
			PageVO pvo) throws Exception {
		// 로그인 성공 시 세션에 로그인한 id가 등록되도록 설정
		// 설정한 userId 를 session.getAttribute("userId")로 등록한 id를 가져올 수 있도록 코드 작성
		String userId = (String) session.getAttribute("userId");
		// String userId 변수는 로그인했을 때의 아이디를 담을 수 있는 기억공간 변수이다.
		if (pvo.getPage() == null) {
			pvo.setPage(1);
			pvo.setTotalCount(mcsvs.listCount(m_name));
		} else {
		}
		model.addAttribute("m_detail", msvs.m_detail(m_name));
		pvo.setTotalCount(mcsvs.listCount(m_name));
		pvo.calcPage();
		model.addAttribute("listSize", mcsvs.listCount(m_name));
		model.addAttribute("u_id", userId); // 세션에 저장된 아이디 jsp 파일로 넘겨줌,
		model.addAttribute("commentList", mcsvs.selectComment(pvo));
		model.addAttribute("pageVO", pvo);
		// 총 리스트의 사이즈를 구하는 매퍼를 만들어야됨.
		return "m_detail_view";
	}

	// 영화자세히보기 _ 댓글달기
	@RequestMapping(value = "/m_detail_c", method = RequestMethod.GET)
	public String movie_input_c(Locale locale, Model model, @ModelAttribute("") m_commentVO mcvo) throws Exception {
		mcsvs.insert(mcvo);
		String encodedMName = URLEncoder.encode(mcvo.getM_name(), "UTF-8");
		System.out.println("댓글달기 후 영화이름 " + mcvo.getM_name());
		return "redirect:/m_detail?m_name=" + encodedMName;
	}

	// 영화자세히보기 _ 댓글삭제
	@RequestMapping(value = "/m_detaile_c_remove", method = RequestMethod.GET)
	public String m_detaile_c_remove(Locale locale, Model model, @ModelAttribute("") @RequestParam("u_id") String u_id,
			@RequestParam("c_comment") String c_comment, @RequestParam("m_name") String m_name) throws Exception {
		mcsvs.delC(u_id, c_comment);
		String encodedMName = URLEncoder.encode(m_name, "UTF-8");
		return "redirect:/m_detail?m_name=" + encodedMName;
	}

	// 주변 영화관 검색.
	@RequestMapping(value = "/m_nearbyCinema", method = RequestMethod.GET)
	public String m_nearbyCinema(Locale locale, Model model, HttpSession s) throws Exception {
		String nowId = (String) s.getAttribute("userId");
		String nowId_addr = usvs.myInformation(nowId).getU_addr().split(",")[1];
		model.addAttribute("loginUser", usvs.myInformation(nowId));
		model.addAttribute("nowId_addr", nowId_addr);
		return "m_nearbyCinema";
	}

}