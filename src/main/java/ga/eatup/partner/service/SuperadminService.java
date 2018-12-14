package ga.eatup.partner.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ga.eatup.partner.domain.NoticeVO;
import ga.eatup.user.domain.MenuVO;

@Service
public interface SuperadminService {
	
	public MenuVO searchSno(String sname, String mname);
	
	public int menuAdd(MenuVO vo);
	
	public MenuVO searchSnoMno(String sname, String mname);
	
	public int menuUpdate(MenuVO vo);
	
	public int menuRemove(int mno);
	
	public int noticeAdd(NoticeVO vo);
	
	public List<NoticeVO> noticeList();

}
