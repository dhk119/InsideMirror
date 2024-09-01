package kr.co.iei.guestbook.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.co.iei.guestbook.dto.GuestBook;
import kr.co.iei.guestbook.service.GuestBookService;
import kr.co.iei.member.model.dto.Member;
import kr.co.iei.member.model.service.MemberService;


@Controller
@RequestMapping("/guest")
public class GuestBookController {
    @Autowired
    private GuestBookService guestBookService; 
    

    
    public GuestBookController(GuestBookService guestBookService)
    {
    	this.guestBookService = guestBookService;
    }
   
    @GetMapping(value="/guestbookList")
    public String guestbookList(Integer memberNo, Model model) {
    	GuestBook gb = new GuestBook();
    	gb.setMemberNo(memberNo);
        List<GuestBook> guestbookList = guestBookService.getAllComments(gb);
        
        model.addAttribute("guestbookList", guestbookList);
        
      
        return "guest/guestbookList";
    }
//    @GetMapping(value="/guestList")
//    public String guestList(int memberNo, Model model) {
//    	System.out.println("memberNo : " + memberNo);
//    	return "guest/guestList";
//    }
    

    @PostMapping(value="/insertComment")
    public String insertComment(Integer guestBookType, String guestbookInput, Integer memberNo, Integer guestWriterNo, String guestNickname, Model model) {  	
        GuestBook gb = new GuestBook();
        
        gb.setGuestBookType(guestBookType);
        gb.setGuestWriterNo(guestWriterNo);
        gb.setMemberNo(memberNo);
        gb.setGuestCommentContent(guestbookInput);
        gb.setGuestNickName(guestNickname);
        
        // 익명일 경우
        if (guestBookType == 0) {
            gb.setGuestNickName("익명");
        } else { // 닉네임일 경우
            gb.setGuestNickName(guestNickname);
        }

        int result = guestBookService.insertComment(gb);
       // System.out.println("Service 결과: " + result);
        
        if (result > 0) {
        	String successlog = "/guest/guestbookList?memberNo=" + memberNo;
            model.addAttribute("title", "댓글 작성");
            model.addAttribute("msg", "댓글이 작성되었습니다.");
            model.addAttribute("icon", "success");
            model.addAttribute("loc", successlog);
        } else {
        	String errorlog = "/guest/guestbookList?memberNo=" + memberNo;
            model.addAttribute("title", "댓글 작성 실패");
            model.addAttribute("msg", "댓글 작성 중 문제가 발생했습니다.");
            model.addAttribute("icon", "warning");
            model.addAttribute("loc");
        }
        return "common/msg";
    }

    
    


	
	@PostMapping(value="/updateComment")
    public String updateComment(String guestCommentContent, Integer guestCommentNo, Model model) {
		if (guestCommentNo == null) {
	        model.addAttribute("title", "실패");
	        model.addAttribute("msg", "유효하지 않은 댓글 번호입니다.");
	        model.addAttribute("icon", "warning");
	        model.addAttribute("loc", "/guest/guestbookList");
	        return "common/msg";
	    }
		
		GuestBook gb = new GuestBook();
        gb.setGuestCommentContent(guestCommentContent);
        gb.setGuestCommentNo(guestCommentNo);
        int result = guestBookService.updateComment(gb);
        if(result > 0) {
            model.addAttribute("title", "성공");
            model.addAttribute("msg", "댓글이 수정되었습니다.");
            model.addAttribute("icon", "success");
        } else {
            model.addAttribute("title", "실패");
            model.addAttribute("msg", "댓글 수정 중 문제가 발생했습니다.");
            model.addAttribute("icon", "warning");
        }
        model.addAttribute("loc", "/guest/guestbookList");
        return "common/msg";
    }

    @PostMapping(value="/deleteComment")
    public String deleteComment(Integer guestCommentNo, Model model) {
        System.out.println(guestCommentNo);
        GuestBook gb = new GuestBook();
        gb.setGuestCommentNo(guestCommentNo);
        int result = guestBookService.deleteComment(gb);
        if(result > 0) {
            model.addAttribute("title", "성공");
            model.addAttribute("msg", "댓글이 삭제되었습니다.");
            model.addAttribute("icon", "success");
        } else {
            model.addAttribute("title", "실패");
            model.addAttribute("msg", "댓글 삭제 중 문제가 발생했습니다.");
            model.addAttribute("icon", "warning");
        }
        model.addAttribute("loc", "/guest/guestbookList");
        return "common/msg";
    }

    @GetMapping(value="/getComments")
    public List<GuestBook> getComments(Integer memberNo) {
        GuestBook gb = new GuestBook();
        gb.setMemberNo(memberNo);
        List<GuestBook> comments = guestBookService.getAllComments(gb);
        return comments;
    }
    

}
