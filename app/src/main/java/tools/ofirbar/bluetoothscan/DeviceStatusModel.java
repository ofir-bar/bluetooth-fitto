package tools.ofirbar.bluetoothscan;

public class DeviceStatusModel {

    private String[] mFittoResponseSplitted;
    private static final String HEX_DELIMETER = "-";

    public DeviceStatusModel(String response){
        mFittoResponseSplitted = response.split(HEX_DELIMETER);
    }


    public void getResponseStatus(){
        System.out.println(mFittoResponseSplitted[0]);
    }

    public byte[] getResponseSize(){
        return mFittoResponseSplitted[1].getBytes();
    }

    public byte[] getDeviceStatus(){
        return (mFittoResponseSplitted[2]).getBytes();
    }

    public byte[] getHardwareVersion(){
        StringBuilder sb = new StringBuilder();
        for (int i = 3; i < 7; i++){
            sb.append(mFittoResponseSplitted[i]);
        }
        return sb.toString().getBytes();
    }

    public byte[] getBootloaderVersion(){
        StringBuilder sb = new StringBuilder();
        for (int i = 7; i < 11; i++){
            sb.append(mFittoResponseSplitted[i]);
        }
        return sb.toString().getBytes();
    }

    public byte[] getVersionMajor(){
        return (mFittoResponseSplitted[11]).getBytes();
    }

    public byte[] getVersionMinor(){
        return (mFittoResponseSplitted[12]).getBytes();
    }

    public byte[] getVersionBuild(){
        return (mFittoResponseSplitted[13] + mFittoResponseSplitted[14]).getBytes();
    }

    public byte[] getVersionStr(){

        StringBuilder sb = new StringBuilder();
        for (int i = 15; i < mFittoResponseSplitted.length; i++){
            sb.append(mFittoResponseSplitted[i]);
        }
        return sb.toString().getBytes();
    }






}


// 10-15-00-09-00-00-01-05-00-00-01-01-00-af-00-31-2e-30-2e-31-37-35-00