package jp.ac.sinet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MessageJson {

    public String sensor;
    double longitude = 0.0, latitude = 0.0;
    public String time;
//    public List<Double> list_value;
    double value;

//    public void setList(List<Double> list) {
//        this.list_value = list;
//    }
    public String timezone(String zone) {
        // TODO Auto-generated method stub
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXX");
        sdf.setTimeZone(TimeZone.getTimeZone(zone));
        System.out.println(sdf.format(new Date()));
        return sdf.format(new Date());
    }
}
