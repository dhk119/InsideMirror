package kr.co.iei.customer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import kr.co.iei.customer.dto.Customer;
import kr.co.iei.customer.dto.CustomerListData;
import kr.co.iei.customer.service.CustomerService;
import kr.co.iei.member.model.dto.Member;

@Controller
@RequestMapping(value="/customer")
public class CustomerController {
	@Autowired
	private CustomerService customerService;
	
	@GetMapping(value="/customerList")
	public String customerList(Model model,int reqPage, @SessionAttribute Member member) {

		CustomerListData cld = customerService.selectCustomerList(reqPage, member);
		model.addAttribute("list", cld.getList());
		model.addAttribute("pageNavi", cld.getPageNavi());
		return "customer/customerList";
	}
	@GetMapping(value="/customerFrm")
	public String customerFrm() {
		return "customer/customerFrm";
	}
	@PostMapping(value="/customerWrite")
	public String customerWrite(Customer c,@SessionAttribute("member") Member member, Model model) {

		int result = customerService.insertCustomerInq(c,member);
		if(result > 0) {
			// 세션 업데이트 하기
			model.addAttribute("title", "문의");
			model.addAttribute("msg", "5 ~ 10일 정도 걸려요~");
			model.addAttribute("icon", "success");
			model.addAttribute("loc", "/customer/customerList?reqPage=1");
			return "common/msg";
		}else {
			model.addAttribute("title", "실패");
			model.addAttribute("msg", "문의 실패");
			model.addAttribute("icon", "error");
			model.addAttribute("loc", "/customer/customerList?reqPage=1");
			return "common/msg";
		}
	}
	@GetMapping(value="/customerPage")
	public String customerView(Model model, int inqNo) {
		Customer customer = customerService.selectCustomerContent(inqNo); // 임시로 1해둠 나중에 세션처리 시 멤버 번호 보내야함
		model.addAttribute("customer",customer);
		return "/customer/customerPage";
	}
	
}
