package tools.ofirbar.bluetoothscan;

public class DeviceStatusModel {

    private byte[] mFittoResponseSplitted;

    public DeviceStatusModel(byte[] response){
        mFittoResponseSplitted = response;
    }


    public byte[] getResponseStatus(){
        return new byte[]{mFittoResponseSplitted[0], mFittoResponseSplitted[1]};
    }

    public byte[] getResponseSize(){
        return new byte[]{mFittoResponseSplitted[2], mFittoResponseSplitted[3]};
    }

    public byte[] getDeviceStatus(){
        return new byte[]{mFittoResponseSplitted[4], mFittoResponseSplitted[5]};
    }

    public byte[] getHardwareVersion(){
        byte[] ba = new byte[8];

        for (int i = 6; i < 14; i++){
            ba[i-6] = mFittoResponseSplitted[i];
        }

        return ba;
    }

    public byte[] getBootloaderVersion(){
        byte[] ba = new byte[8];

        for (int i = 14; i < 22; i++){
            ba[i-14] = mFittoResponseSplitted[i];
        }
        return ba;
    }

    public byte[] getVersionMajor(){
        return new byte[]{mFittoResponseSplitted[22], mFittoResponseSplitted[23]};
    }

    public byte[] getVersionMinor(){
        return new byte[]{mFittoResponseSplitted[24], mFittoResponseSplitted[25]};
    }

    public byte[] getVersionBuild(){
        return new byte[]{mFittoResponseSplitted[26], mFittoResponseSplitted[27], mFittoResponseSplitted[28], mFittoResponseSplitted[29]};
    }

    public String getVersionStr(){

        // Extract the right bytes
        byte[] ba = new byte[7];

        for (int i = 15; i < 22; i++){
            ba[i-15] = mFittoResponseSplitted[i];
        }
        // Encode the bytes as String and return it
        return new String(ba);

    }

    // Use this String example for reference
    // String value = "10 15 00 09 00 00 01 05 00 00 01 01 00 af 00 31 2e 30 2e 31 37 35 00";

}