package com.enjoylearning.lison.mongodb;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class RondomDateTest {
	
	   @Test
	   public void testRondomDate() {  
		   for(int i=0;i<=10000;i++){
//		        Date date = randomDate("2015-01-01","2017-10-31");  
//		        System.out.println(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(date));
			   BigDecimal test = randomBigDecimal(10000,1); 
		        System.out.println(test.toString());
		   }
	    }  
	      
	      
	    /** 
	     * 获取随机日期 
	     * @param beginDate 起始日期，格式为：yyyy-MM-dd 
	     * @param endDate 结束日期，格式为：yyyy-MM-dd 
	     * @return 
	     */  
	    public static Date randomDate(String beginDate,String endDate){  
	        try {  
	            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");  
	            Date start = format.parse(beginDate);  
	            Date end = format.parse(endDate);  
	              
	            if(start.getTime() >= end.getTime()){  
	                return null;  
	            }  
	              
	            long date = random(start.getTime(),end.getTime());  
	              
	            return new Date(date);  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
	        return null;  
	    }  
	      
	    private static long random(long begin,long end){  
	        long rtn = begin + (long)(Math.random() * (end - begin));  
	        if(rtn == begin || rtn == end){  
	            return random(begin,end);  
	        }  
	        return rtn;  
	    } 
	    
	    
	    public static BigDecimal randomBigDecimal(float max,float min) {  
//	        float Max = 10000, Min = 1.0f;  
	        BigDecimal db = new BigDecimal(Math.random() * (max - min) + min);  
	        return db.setScale(2, BigDecimal.ROUND_HALF_UP);// 保留30位小数并四舍五入  
	    }  
	    
	    
}
