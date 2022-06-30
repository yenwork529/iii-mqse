package org.iii.esd.battery;

import org.iii.esd.battery.brand.AecService;
import org.iii.esd.battery.brand.BrandService;
import org.iii.esd.battery.brand.ChemService;
import org.iii.esd.battery.brand.JosephService;
import org.iii.esd.battery.brand.SungrowService;
import org.iii.esd.battery.config.Brand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BatteryService {

	@Autowired
	private AecService aecService;	
	
	@Autowired
	private ChemService chemService;		
	
	@Autowired
	private JosephService josephService;
	
	@Autowired
	private SungrowService sungrowService;

	public BrandService getBrandService(Brand brand) {
		switch (brand) {
			case AEC:
				return aecService;
			case CHEM:
				return chemService;
			case JOSEPH:
				return josephService;
			case SUNGROW:
				return sungrowService;				
		}
		return null;
	}

}