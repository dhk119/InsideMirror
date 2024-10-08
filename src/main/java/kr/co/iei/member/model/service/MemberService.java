package kr.co.iei.member.model.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.iei.guestbook.dto.GuestBook;
import kr.co.iei.member.model.dao.MemberDao;
import kr.co.iei.member.model.dto.Member;
import kr.co.iei.member.model.dto.MemberListData;
import kr.co.iei.member.model.dto.Title;
import kr.co.iei.product.dao.ProductDao;

@Service
public class MemberService {
	@Autowired
	private MemberDao memberDao;
	@Autowired 
	private ProductDao productDao;
	
	public MemberListData selectAllMember(int reqPage) {
		int numPerPage = 5;
		
		int end = reqPage * numPerPage;
		int start = end - numPerPage + 1;
		
		List member = memberDao.selectAllMember(start, end);
		
		int totalCount = memberDao.selectAllMemberTotalCount();
		
		int totalPage = 0;
		if(totalCount % numPerPage == 0) {
			totalPage = totalCount/numPerPage;
		}else {
			totalPage = totalCount/numPerPage + 1;
		}//else
		
		int pageNaviSize = 5;
		int pageNo = ((reqPage - 1)/pageNaviSize) * pageNaviSize + 1;
		
		String pageNavi = "<ul class='pagination circle-style'>";
		
		if (pageNo != 1) {
			pageNavi += "<li>";
			pageNavi += "<a class='page-item' href='/admin/allMember?reqPage=" + (pageNo - 1) + "'>";
			pageNavi += "<span class='material-icons'>chevron_left</span>";
			pageNavi += "</a></li>";
		}//if
		
		for(int i = 0; i < pageNaviSize; i++){
			pageNavi += "<li>";
			if(pageNo == reqPage){
				pageNavi += "<a class='page-item active-page' href='/admin/allMember?reqPage="+pageNo+"'>";
			}
			else{
				pageNavi += "<a class='page-item' href='/admin/allMember?reqPage="+pageNo+"'>";
			}//else
			
			pageNavi += pageNo;
			pageNavi += "</a></li>";
			pageNo++;
			
			if(pageNo > totalPage) break;
		}//for
		
		if(pageNo <= totalPage){
			pageNavi += "<li>";
			pageNavi += "<a class='page-item' href='/admin/allMember?reqPage="+pageNo+"'>";
			pageNavi += "<span class='material-icons'>chevron_right</span>";
			pageNavi += "</a></li>";
		}//if

		pageNavi += "</ul>";
		
		MemberListData mld = new MemberListData(member, pageNavi);
		
		return mld;
	}//selectAllMember


	@Transactional
	public int insertMember(Member m) {
		int result = memberDao.insertMember(m);
		return result;
	}

	public Member selectOneMember(Member m) {
		Member member = memberDao.selectOneMember(m);
		return member;
	}//selectOneMember

	public Member selectOneMember(String checkNickname) {
		Member member = memberDao.selectOneMember(checkNickname);
		return member;
	}

	@Transactional
	public int resetPassword(Member m) {
		int result = memberDao.resetPassword(m);
		return result;
	}


	public Member checkPassword(String memberPw) {
		Member member = memberDao.checkPassword(memberPw);
		return member;
	}


	//관리자 메인 홈피에서 멤버 5명만 출력
	public List selectFiveMember() {
		List fiveMemberList = memberDao.selectFiveMember();
		return fiveMemberList;
	}//selectFiveMembe


	public Member selectAdminOneMember(int memberNo) {
		Member member = memberDao.selectAdminOneMember(memberNo);
		return member;
	}//selectAdminOneMember


	public List findMember(int memberNo,String findMember) {
		List memberList = memberDao.findMember(memberNo,findMember);
		return memberList;
	}


	public List viewAllMember(int memberNo) {
		List list = memberDao.viewAllMember(memberNo);
		return list;
	}


	public Title getTitle(Member member) {
		List board = memberDao.board(member);
		List photo = memberDao.photo(member);
		List photo1 = memberDao.photo1(member);
		
		Title title = new Title();
		title.setBoard(board);
		title.setPhoto(photo);
		title.setPhoto1(photo1);
		return title;
	}

	@Transactional
	public int updateProfile(Member member) {
		int result = memberDao.updateProfile(member);
		return result;
	}


	public Member selectFriendPage(Member m) {
		Member friendMember = memberDao.selectFriendPage(m);
			
		int result = memberDao.updateTotalCount(m.getMemberNo());				
			
		return friendMember;
	}

	@Transactional
	public int joinProduct(Member m) {
//		Member listNum = memberDao.selectMemberNo(m);
		Member member = memberDao.selectOneMember(m);
		int num = member.getMemberNo();
		/*회원가입 끝나면 기본 배경, 커서 강제 주입*/
		int b = productDao.joinProductB(num);
		int c = productDao.joinProductC(num);
		int f =  productDao.joinProductF(num);
		return 0;
	}


	public int updateMsg(String profileMsg, Member member) {
		int result = memberDao.updateMsg(profileMsg, member);
		return result;
	}



	
	
	
	/*
	 * @Transactional 
	 * public Member updateProfileContent(Member m) { 
	 * int result = memberDao.updateProfileContent(m);
	 *  if(result>0) { Member member = memberDao.selectOneMember(m); 
	 *  return member; 
	 *  } 
	 *  return null; 
	 *  }
	 */
	//사진첩
	public int getTotalCount(int memberNo) {
		int totalCount = memberDao.getTotalCount(memberNo);
		return totalCount;
	}


	public List selectPhotoList(int start, int amount, int friendMemberNo, int memberNo) {
		int end = start + amount - 1;
		List photoList = memberDao.selectPhotoList(start, end, friendMemberNo, memberNo);
		return photoList;
	}


	public Member getFriendMember(int memberNo) {
		Member member = memberDao.getFriendMember(memberNo);
		return member;
	}//친구 조회 (with memberNo)


	public List selectFriendPhotoSort(int start, int amount, int sort, int friendMemberNo, int memberNo) {
		int end = start + amount - 1;
		List photoList = null;
		if(sort == 1){
			photoList = memberDao.selectPhotoList(start, end, friendMemberNo, memberNo);
		}
		else if (sort == 2){
			photoList = memberDao.PhotoSortPopular(start, end, friendMemberNo, memberNo);
		}
		return photoList;
	}//친구 sort


	public int selectFriend(Member m, int memberNo) {
		int selectFriend = memberDao.selectFriend(m, memberNo);
		if(selectFriend == 1) {
			return selectFriend;
		}else {
			return 0;			
		}
	}


	@Transactional
	public int insertFriendMember(int friendMemberNo, String friendMemberNickName, Member member) {
		int result = memberDao.insertFriendMember(friendMemberNo, friendMemberNickName, member);
		if(result > 0) {
			result = memberDao.insertFriendMember(friendMemberNo, member);
		}
		return result;
	}

	@Transactional
	public int deleteFriendMember(int friendMemberNo, Member member) {
		int result = memberDao.deleteFriendMember(friendMemberNo, member);
		if(result > 0) {
			result = memberDao.deleteFriendMember2(friendMemberNo, member);
		}
		return result;
	}


	public List<GuestBook> getAllComments(GuestBook gb) {
		return memberDao.getAllComments(gb);
	}//친구 방명록 조회


	public Member selectOneEmail(String memberId) {
		Member member = memberDao.selectEmail(memberId);
		return member;
	}







	


	
}
