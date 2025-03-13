package com.crypto.trade.common;

import java.text.NumberFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crypto.trade.dao.TradeDao;

@Service
public class CommonMethods {
	
	@Autowired
	TradeDao tradeDao;
	
	public String getDynamicPrecisionValue(String asset) {
		/** get maximum precision value by assetId */
		int maxPrecisionValue = tradeDao.getMaxDesPrecisionValue(asset);
		String zero = "";
		if (maxPrecisionValue != 0) {
			for (int i = 0; i < maxPrecisionValue; i++) {
				zero = zero.concat("0");
			}
		} else {
			zero = "00";
		}
		return zero;
	}
	public String getPrecisionAmount(String amount) {
		String[] amtArray = amount.split("\\.", -1);
		String amt = NumberFormat.getInstance().format(Integer.parseInt(amtArray[0]));
		return amt.concat(".").concat(amtArray[1]);
	}
}
