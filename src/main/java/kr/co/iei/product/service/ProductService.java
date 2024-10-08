package kr.co.iei.product.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.co.iei.customer.dto.CustomerListData;
import kr.co.iei.member.model.dao.MemberDao;
import kr.co.iei.member.model.dto.Member;
import kr.co.iei.member.model.dto.MemberListData;
import kr.co.iei.product.dao.ProductDao;
import kr.co.iei.product.dto.BuyProduct;
import kr.co.iei.product.dto.BuyProductListData;
import kr.co.iei.product.dto.ProductListData;
import kr.co.iei.product.dto.SellBuyProduct;
import kr.co.iei.product.dto.SellProduct;

@Service
public class ProductService {
	@Autowired
	private ProductDao productDao;
	@Autowired
	private MemberDao memberDao;

	@Transactional
	public int updateAcorns(Member m) {
		/* 멤버 테이블에 도토리 넣어주기 */
		int result1 = productDao.updateAcorns(m);

		/* 도토리 구매 이력 테이블에 도토리 정보 넣어주기 */
		int result2 = productDao.insertAcorns(m);

		if (result1 > 0 && result2 > 0) { // 도토리 update / insert 성공 1 반환
			return 1;
		} else { // 도토리 insert 실패 0 반환
			return 0;
		}
	}

	public List selectProductPhoto() {
		List product = productDao.selectProductPhoto();
		return product;
	}

	/* 판매 상품 */
	public ProductListData selectProduct(int reqPage, int type) {
		// 한 페이지당 10개의 글 조회
		int numPerPage = 10;
		// 시작번호
		int end = reqPage * numPerPage; // 1페이지면 10, 2페이지면 20 ...
		int start = end - numPerPage + 1; // 10 - 10 = 1번 부터..., 20 - 10 + 1 = 11 번 부터...
		List list = null;
		int totalCount = 0;
		
		if(type==0) { // 타입0 => 전체 조회
			list = productDao.selectProductList(start, end);
			totalCount = productDao.selectProductTotalCount();
		}else { // 타입 (배경, 커서, 폰트) 조회
			list = productDao.selectProductList(start, end, type);
			totalCount = productDao.selectProductTotalCount(type);
		}
		
		// 전체 페이지 수 계산
		// 전체 페이지 수 계산
		int totalPage = 0;

		if (totalCount % numPerPage == 0) { // 나머지가 없다면
			totalPage = totalCount / numPerPage;
		} else { // 나머지가 있다면
			totalPage = totalCount / numPerPage + 1;
		} // 올림연산, 삼항 연산도 많이 씀
			// 페이지네비 사이즈 조정
		int pageNaviSize = 5;

		// 페이지네비 시작번호를 지정
		// reqPage 1 ~ 5 : 1 2 3 4 5
		// reqPage 6 ~ 10 : 6 7 8 9 10
		// reqPage 11 ~ 15 : 11 12 13 14 15

		int pageNo = ((reqPage - 1) / pageNaviSize) * pageNaviSize + 1;

		// html코드 작성(페이지 네비게이션 작성)
		String pageNavi = "<ul class='pagination circle-style'>";

		// 이전 버튼(1페이지로 시작하지 않으면)
		if (pageNo != 1) {
			pageNavi += "<li>";
			pageNavi += "<a class='page-item' href='/product/productList?reqPage=" + (pageNo - 1) + "'>";
			pageNavi += "<span class='material-icons'>chevron_left</span>";
			pageNavi += "</a></li>";
		}

		for (int i = 0; i < pageNaviSize; i++) {
			pageNavi += "<li>";
			if (pageNo == reqPage) {
				pageNavi += "<a class='page-item active-page' href='/product/productList?reqPage=" + pageNo + "&type="+type+ "'>";
			} else {
				pageNavi += "<a class='page-item' href='/product/productList?reqPage=" + pageNo + "&type="+type+ "'>";
			}
			pageNavi += pageNo;
			pageNavi += "</a></li>";
			pageNo++;

			// 페이지를 만들다가 최종페이지를 출력했으면 더 이상 반복하지 않고 종료
			if (pageNo > totalPage) {
				break;
			}
		}
		// 다음 버튼(최종 페이지를 출력하지 않았으면)
		if (pageNo <= totalPage) {
			pageNavi += "<li>";
			pageNavi += "<a class='page-item' href='/product/productList?reqPage=" + pageNo + "&type="+type+ "'>";
			pageNavi += "<span class='material-icons'>chevron_right</span>";
			pageNavi += "</a></li>";
		}

		// ul 닫기
		pageNavi += "</ul>";
		ProductListData pld = new ProductListData(list, pageNavi);
//		Customer customer = customerDao.selectCustomerList(reqPage);
		return pld;
	}

	@Transactional
	public int addProduct(SellProduct sp) {
		int result = productDao.addProduct(sp);
		return result;
	}// addProduct

	// 상품 상세 페이지 정보 출력
	public SellProduct selectProductInfo(int productNo) {
		SellProduct p = productDao.selectProductInfo(productNo);
		return p;
	}

	/* 구매한 상품 정보 출력 */
	public BuyProduct selectOneProduct(Member member, int productNo) {
		BuyProduct bp = productDao.selectOneProduct(member, productNo);
		return bp;
	}

	// 상품 구매하기 insert
	@Transactional
	public int productAdd(Member m, SellProduct sp) {
		// 회원 정보에서 가지고 있는 도토리 개수 확인하기
		Member member = memberDao.selectOneMember(m);
		
		if (member.getAcorns() >= sp.getProductPrice()) { // 판매중인 도토리 수 보다 멤버가 가지고 있는 도토리 개수가 같거나 많다면 성공
			// 상품 구매 회원 지갑에서 도토리 빼내기 => 성공하면 insert / 실패시 0 반환
			int result = productDao.updateAcornMinus(m, sp);
			if (result > 0) { // 성공하면 구매한 상품에 넣어주기
				int result2 = productDao.insertProductAdd(m, sp);
				if (result2 > 0) { // 구매한 상품에 잘 넣었으면 리턴
					return result2;
				}
				return 0;
			} else {
				// 회원 지갑에서 빼내는거 실패 시 0 반환 => 컨트롤러에서 알림창 띄우기
				return 0;
			}
		} else {
			// 상품 가격이 도토리 수 보다 크다면 ... 실패
			return -10; // 상품 가격이 더 큰건 -10으로 구분
		}

	}

	public ProductListData selectAdminProduct(int reqPage, int type) {
		int numPerPage = 10;
		// 시작번호
		int end = reqPage * numPerPage; // 1페이지면 10, 2페이지면 20 ...
		int start = end - numPerPage + 1; // 10 - 10 = 1번 부터..., 20 - 10 + 1 = 11 번 부터...

		List list = null;
		int totalCount = 0;
		
		if(type == 0) {
			list = productDao.selectProductList(start, end);
			totalCount = productDao.selectProductTotalCount();
		}else {
			list = productDao.selectProductList(start, end, type);
			totalCount = productDao.selectProductTotalCount(type);
		}
		
		int totalPage = 0;
		
		if (totalCount % numPerPage == 0) { // 나머지가 없다면
			totalPage = totalCount / numPerPage;
		} else { // 나머지가 있다면
			totalPage = totalCount / numPerPage + 1;
		}//else
		int pageNaviSize = 5;

		int pageNo = ((reqPage - 1) / pageNaviSize) * pageNaviSize + 1;

		// html코드 작성(페이지 네비게이션 작성)
		String pageNavi = "<ul class='pagination circle-style'>";

		// 이전 버튼(1페이지로 시작하지 않으면)
		if (pageNo != 1) {
			pageNavi += "<li>";
			pageNavi += "<a class='page-item' href='/admin/adminProductList?reqPage=" + pageNo + "&type="+type+ "'>";
			pageNavi += "<span class='material-icons'>chevron_left</span>";
			pageNavi += "</a></li>";
		}

		for (int i = 0; i < pageNaviSize; i++) {
			pageNavi += "<li>";
			if (pageNo == reqPage) {
				pageNavi += "<a class='page-item active-page' href='/admin/adminProductList?reqPage=" + pageNo + "&type="+type+ "'>";
			} else {
				pageNavi += "<a class='page-item' href='/admin/adminProductList?reqPage=" + pageNo + "&type="+type+ "'>";
			}
			pageNavi += pageNo;
			pageNavi += "</a></li>";
			pageNo++;

			// 페이지를 만들다가 최종페이지를 출력했으면 더 이상 반복하지 않고 종료
			if (pageNo > totalPage) {
				break;
			}
		}
		// 다음 버튼(최종 페이지를 출력하지 않았으면)
				if (pageNo <= totalPage) {
					pageNavi += "<li>";
					pageNavi += "<a class='page-item' href='/admin/adminProductList?reqPage=" + pageNo + "&type="+type+ "'>";
					pageNavi += "<span class='material-icons'>chevron_right</span>";
					pageNavi += "</a></li>";
				}

		// ul 닫기
		pageNavi += "</ul>";
		ProductListData pld = new ProductListData(list, pageNavi);
		return pld;
	}// selectAdminProduct

	@Transactional
	public int productUpdate(SellProduct sp) {
		int result = productDao.productUpdate(sp);
		return result;
	}// productUpdate

	public ProductListData selectBuyProduct(int reqPage,  Member member, int type, String product) {
		// 한 페이지당 10개의 글 조회
		int numPerPage = 10;
		// 시작번호
		int end = reqPage * numPerPage; // 1페이지면 10, 2페이지면 20 ...
		int start = end - numPerPage + 1; // 10 - 10 = 1번 부터..., 20 - 10 + 1 = 11 번 부터...
		
		List list = null;
		int totalCount = 0;
		
		if(type==0) { // 타입0 => 전체 조회
			list = productDao.selectBuyProductList(start, end, member);
			totalCount = productDao.selectBuyProductTotalCount(member);
		}else { // 타입 (배경, 커서, 폰트) 조회
			list = productDao.selectBuyProductList(start, end, member, type);
			totalCount = productDao.selectBuyProductTotalCount(member, type);
		}
		

		// 전체 페이지 수 계산
		int totalPage = 0;

		if (totalCount % numPerPage == 0) { // 나머지가 없다면
			totalPage = totalCount / numPerPage;
		} else { // 나머지가 있다면
			totalPage = totalCount / numPerPage + 1;
		} // 올림연산, 삼항 연산도 많이 씀
			// 페이지네비 사이즈 조정
		int pageNaviSize = 5;

		// 페이지네비 시작번호를 지정
		// reqPage 1 ~ 5 : 1 2 3 4 5
		// reqPage 6 ~ 10 : 6 7 8 9 10
		// reqPage 11 ~ 15 : 11 12 13 14 15

		int pageNo = ((reqPage - 1) / pageNaviSize) * pageNaviSize + 1;

		// html코드 작성(페이지 네비게이션 작성)
		String pageNavi = "<ul class='pagination circle-style'>";

		// 이전 버튼(1페이지로 시작하지 않으면)
		if (pageNo != 1) {
			pageNavi += "<li>";
			pageNavi += "<a class='page-item' href='/product/buyProductList?reqPage=" + (pageNo - 1) + "&type="+type+"&product="+product+ "'>";
			pageNavi += "<span class='material-icons'>chevron_left</span>";
			pageNavi += "</a></li>";
		}

		for (int i = 0; i < pageNaviSize; i++) {
			pageNavi += "<li>";
			if (pageNo == reqPage) {
				pageNavi += "<a class='page-item active-page' href='/product/buyProductList?reqPage=" + pageNo + "&type="+type+"&product="+product+ "'>";
			} else {
				pageNavi += "<a class='page-item' href='/product/buyProductList?reqPage=" + pageNo + "&type="+type+"&product="+product+ "'>";
			}
			pageNavi += pageNo;
			pageNavi += "</a></li>";
			pageNo++;

			// 페이지를 만들다가 최종페이지를 출력했으면 더 이상 반복하지 않고 종료
			if (pageNo > totalPage) {
				break;
			}
		}
		// 다음 버튼(최종 페이지를 출력하지 않았으면)
		if (pageNo <= totalPage) {
			pageNavi += "<li>";
			pageNavi += "<a class='page-item' href='/product/buyProductList?reqPage=" + pageNo + "&type="+type+"&product="+product+ "'>";
			pageNavi += "<span class='material-icons'>chevron_right</span>";
			pageNavi += "</a></li>";
		}

		// ul 닫기
		pageNavi += "</ul>";
		ProductListData pld = new ProductListData(list, pageNavi);
//		Customer customer = customerDao.selectCustomerList(reqPage);
		return pld;
	}


	@Transactional
	public int adminAddProduct(SellProduct sp) {
		int result = productDao.adminAddProduct(sp);
		return result;
	}// adminAddProduct

	@Transactional
	public int productDelete(int productNo) {
		int result = productDao.productDelete(productNo);
		return result;
	}// productDelete


	public BuyProductListData selectRefundList(int reqPage) {
		int numPerPage = 5;

		int end = reqPage * numPerPage;
		int start = end - numPerPage + 1;

		List refund = productDao.selectRefundList(start, end);

		int totalCount = productDao.selectRefundListTotalCount();

		int totalPage = 0;
		if (totalCount % numPerPage == 0) {
			totalPage = totalCount / numPerPage;
		} else {
			totalPage = totalCount / numPerPage + 1;
		} // else

		int pageNaviSize = 5;
		int pageNo = ((reqPage - 1) / pageNaviSize) * pageNaviSize + 1;

		String pageNavi = "<ul class='pagination circle-style'>";

		if (pageNo != 1) {
			pageNavi += "<li>";
			pageNavi += "<a class='page-item' href='/admin/adminRefundList?reqPage=" + (pageNo - 1) + "'>";
			pageNavi += "<span class='material-icons'>chevron_left</span>";
			pageNavi += "</a></li>";
		} // if

		for (int i = 0; i < pageNaviSize; i++) {
			pageNavi += "<li>";
			if (pageNo == reqPage) {
				pageNavi += "<a class='page-item active-page' href='/admin/adminRefundList?reqPage=" + pageNo + "'>";
			} else {
				pageNavi += "<a class='page-item' href='/admin/adminRefundList?reqPage=" + pageNo + "'>";
			} // else

			pageNavi += pageNo;
			pageNavi += "</a></li>";
			pageNo++;

			if (pageNo > totalPage)
				break;
		} // for

		if (pageNo <= totalPage) {
			pageNavi += "<li>";
			pageNavi += "<a class='page-item' href='/admin/adminRefundList?reqPage=" + pageNo + "'>";
			pageNavi += "<span class='material-icons'>chevron_right</span>";
			pageNavi += "</a></li>";
		} // if

		pageNavi += "</ul>";

		BuyProductListData bld = new BuyProductListData(refund, pageNavi);

		return bld;
	}//selectRefundList

	// 구매한 상품 상세보기 정보 출력
	public SellBuyProduct selectBuyProductInfo(int buyNo) {
		SellBuyProduct sp = productDao.selectBuyProductInfo(buyNo);
		return sp;
	}

	//관리자 메인 홈피에서 상품 3개 출력
	public List selectThreeProduct() {
		List threeProductList = productDao.selectThreeProduct();
		return threeProductList;
	}//selectThreeProduce

	// 사용중인 상품 출력
	public List selectUseProductInfo(Member member) {
		List list = productDao.selectUseProductInfo(member);
		return list;
	}

	@Transactional
	public int updateUseProduct(int productNo, int productListNo, Member member) {
		// 적용하기 누른 상품의 리스트 번호 0으로...
		int result = productDao.updateZeroProduct(member, productListNo, 0);
		if(result > 0) { // 기존 상품 초기화에 성공하면
			int r = productDao.updateUseProduct(productNo, productListNo, member);
			return r;
		}else { // 기존 상품 초기화 성공 실패시...
			return 0;
		}
	}

	// 모두 초기화
	@Transactional
	public int updateZeroProduct(Member member) {
		// 지금 적용 중인 상품 모두 0으로
		int result = productDao.updateAllZeroProduct(member);
		if(result > 0) {
			// 기본 배경, 커서, 폰트 지정
			int r = productDao.updateProductB(member, 1); // 기본 배경
			int c = productDao.updateProductC(member, 2); // 기본 커서
			int f = productDao.updateProductF(member, 3); // 기본 폰트
			if(r > 0 && c > 0 && f>0) {
				return result;
			}else {
				return 0;
			}
		}else {
			return 0;
		}
	}
	
	// 하나만 초기화
	@Transactional
	public int updateOneZeroProduct(Member member, int productNo, int productListNo) {
		// 누른 상품 리스트 0으로 만들기
		int result = productDao.updateOneZeroProduct(member,productListNo);
		if(result > 0) { // 누른 상품 리스트 0으로 초기화 시 성공 헤ㅐㅆ을 때
			
			if(productListNo==1) { // 배경 초기화
				int r = productDao.updateProductB(member, 1);
			}else if(productListNo==2) { // 커서 초기화
				int c = productDao.updateProductC(member, 2);
			}else { // 폰트 초기화
				int f = productDao.updateProductF(member, 3);
			}
		}else {
			return 0;
		}
		return result;
	}


}
