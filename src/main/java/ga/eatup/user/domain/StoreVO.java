package ga.eatup.user.domain;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class StoreVO {

	private int sno, pno, mno;
	private String sname, saddress, b_field, businessHours, telephone;
	private String qr_uuid, qu_fname;
	private double lat, lng;
	private Date regdate;
	private String uuid;
	private String upload_path;
	private String fname;
	
	private List<MenuVO> menuList;
}
