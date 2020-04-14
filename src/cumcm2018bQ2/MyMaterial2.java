package cumcm2018bQ2;

public class MyMaterial2 {
    int AssignedCNC;//0~7
    int upTime;//上料完成时间
    int downTime;//下料和清洗完成时间
//    int Status;//加工中，可下料，（清洗中）(或者由外部容器指定)
    int waitTime;//（冗余）
    public MyMaterial2(int CurrentTime, int CNCnum){//上料时也表示生成了这个物料对象,传入时间为刚执行上料时时间，不包括上料耗时
        this.upTime=CurrentTime;
        this.waitTime=560+(CNCnum%2==0?28:31);//（冗余）
        this.AssignedCNC=CNCnum;
    }
//    public void update(int CurrentTime){
//        waitTime-=(CurrentTime-upTime);
//    }
//    public  void downThis(int CurrentTime){
//        downTime=CurrentTime;
//    }
}
