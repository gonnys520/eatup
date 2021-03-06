package ga.eatup.partner.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import ga.eatup.user.domain.MenuVO;
import lombok.extern.java.Log;
import net.coobird.thumbnailator.Thumbnailator;

@Controller
@Log
@RequestMapping("/upload/*")
public class UploadController {

	@PostMapping("/deleteFile")
	@ResponseBody
	public ResponseEntity<String> deleteFile(String fileName, String type){
		log.info("deleteFile: " + fileName);
		
		File file;
		
		try {
			file = new File("C:\\upload\\" + URLDecoder.decode(fileName, "UTF-8"));
			file.delete();
			
			if(type.equals("image")) {
				String largeFileName = file.getAbsolutePath().replace("s_", "");
				
				log.info("largeFileName: " + largeFileName);
				
				file = new File(largeFileName);
				
				file.delete();
			}
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<String>("deleted",HttpStatus.OK);
	}
	
	@GetMapping(value="/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody
	public ResponseEntity<Resource> downloadFile(@RequestHeader("User-Agent")String userAgent,String fileName){
		
		Resource resource = new FileSystemResource("C:\\upload\\"+ fileName);
		
		if(resource.exists() == false){
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		String resourceName = resource.getFilename();
		
		//remove UUID 
		String resourceOriginalName = resourceName.substring(resourceName.indexOf("_")+1);
		
		HttpHeaders headers = new HttpHeaders();
		
		try {
			
			String downloadName = null;
			
			if(userAgent.contains("Trident")) {
				log.info("IE browser");
				downloadName = URLEncoder.encode(resourceOriginalName ,"UTF-8").replaceAll("\\+"," ");
			}else if(userAgent.contains("Edge")) {
				log.info("Edge browser");
				downloadName = URLEncoder.encode(resourceOriginalName, "UTF-8");
			}else {
				log.info("Chrome broswer");
				downloadName = new String(resourceOriginalName.getBytes("UTF-8"), "ISO-8859-1");
			}
			
			log.info("downloadName: " + downloadName);
			
			headers.add("Content-Disposition", "attachment; filename=" + downloadName);
			
		} catch (UnsupportedEncodingException e) {
			
			e.printStackTrace();
		}
		
		return new ResponseEntity<Resource>(resource, headers, HttpStatus.OK);
	}
	
	@GetMapping("/display")
	@ResponseBody
	public ResponseEntity<byte[]> getFile(String fileName){
		
		log.info("fileName: " + fileName);
		
		File file = new File("C:\\upload\\" + fileName);
		
		log.info("file : " + file);
		
		ResponseEntity<byte[]> result =null;
		
		try {
			HttpHeaders header = new HttpHeaders();
			
			header.add("Content-Type", Files.probeContentType(file.toPath()));
			result = new ResponseEntity<byte[]>(FileCopyUtils.copyToByteArray(file),header, HttpStatus.OK);
		}catch(Exception e) {
			e.printStackTrace();
		}//end catch
		
		return result;
	}
	
	private boolean checkImageType(File file) {
		
		String contentType;
		try {
			contentType = Files.probeContentType(file.toPath());
			return contentType.startsWith("image");
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}
	
	private String getFolder() {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		Date date = new Date();
		
		String str = sdf.format(date);
		
		return str.replace("-", File.separator);
	}
	
	@PostMapping(value = "/uploadAjaxAction", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public ResponseEntity<List<MenuVO>> uploadAjaxPost(MultipartFile[] uploadfile) {
		log.info("update ajax post..........");
		
		List<MenuVO> list = new ArrayList<>();
		
		String uploadFolder = "C:\\upload";
		
		String uploadFolderPath = getFolder();
		//make folder
		File upload_path = new File(uploadFolder, uploadFolderPath);
		log.info("upload path: " + upload_path);
		
		if(upload_path.exists() == false) {
			upload_path.mkdirs();
		}
		
		
		for(MultipartFile file : uploadfile) {
			log.info("----------------");
			log.info("upload file name: " + file.getOriginalFilename());
			log.info("upload file size: " + file.getSize());
			
			MenuVO menuVO = new MenuVO();
			
			String uploadFileName = file.getOriginalFilename();
			
			uploadFileName = uploadFileName.substring(uploadFileName.lastIndexOf("\\")+1);
			log.info("only file name: " + uploadFileName);
			menuVO.setFname(uploadFileName);
			
			UUID uuid = UUID.randomUUID();
			
			uploadFileName = uuid.toString() + "_" + uploadFileName;
			
			try {
				File saveFile = new File(upload_path, uploadFileName);
				file.transferTo(saveFile);
				
				menuVO.setUuid(uuid.toString());
				menuVO.setUpload_path(uploadFolderPath);
				
				//image ��������üũ
				if(checkImageType(saveFile)) {
					
					menuVO.setImage(true);
					
					FileOutputStream thumbnail = new FileOutputStream(new File(upload_path,"s_" + uploadFileName));
					
					Thumbnailator.createThumbnail(file.getInputStream(), thumbnail, 200,200);
					
					thumbnail.close();

				}
				
				list.add(menuVO);
				
				
			}catch(Exception e) {
				e.printStackTrace();
			}//end catch
			
			
		}//end for
		return new ResponseEntity<>(list,HttpStatus.OK);
		
	}
}
