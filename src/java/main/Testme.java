import ucar.nc2.dataset.NetcdfDataset;


public class Testme {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		//String url = "http://aurapar1.ecs.nasa.gov/opendap/hyrax/GOSAT_TANSO_Level2/ACOS_L2S.3.3/2013/001/acos_L2s_130101_07_Production_v150151_L2s30300_r01_PolB_130225032330.h5";
		String url = "http://acdisc.gsfc.nasa.gov/opendap/Aqua_AIRS_Level3/AIRX3STM.005/2004/AIRS.2004.03.01.L3.RetStd031.v5.0.14.0.G07270011247.hdf";
		NetcdfDataset ncd = NetcdfDataset.openDataset(url);
		System.out.println(ncd);
		

	}

}
