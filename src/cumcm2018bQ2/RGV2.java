package cumcm2018bQ2;

import java.util.ArrayList;
import java.util.List;

public class RGV2 {//所有时间采用“秒”单位
    public static int CurrentTime;//-TODO 每次操作需要更新
    public static int RGVposition;//0~3，//-TODO 每次移动操作需要更新
    public static List<CNC2> SignialQueue;//-TODO 每次操作需要更新
    public static List<MyMaterial2> Upmaterials;//使用添加MyMaterial并执行其构造函数表示该物料处于加工中 -TODO 每次操作需要更新
    public static List<MyMaterial2> Downmaterials;//调用完downThis后添加进去，用于记录
    public static Boolean callClean=false;
    public static int stepT1=20;
    public static int stepT2=33;
    public static int stepT3=46;
    public static int fastT=28;
    public static int slowT=31;
    public static int workT1=200;
    public static int workT2=200;
    public static int cleanT=25;
    public static void initDatas(){
        CurrentTime=0;
        RGVposition = 0;
        SignialQueue=new ArrayList<>();
        for (int i = 0; i < 8; ++i) {
            SignialQueue.add(new CNC2(i,RGVposition,fastT,slowT,workT1));
        }
        Upmaterials=new ArrayList<>();
        Downmaterials=new ArrayList<>();
    }
    public static void Assignment2(int MaxTime){//只对CNC贪心
        int opsCount=0;
        int allWaitTime=0;
        while (CurrentTime<=MaxTime){//每次循环RGV进行一次操作并进行更新，
            //按优先级选择对应操作，每次操作后更新
            System.out.println("-------当前时间："+CurrentTime+",第"+(++opsCount)+"次操作开始：");
            if (callClean){
                System.out.println("====清洗操作====");
                callClean=false;
                update(cleanT,-1);
                continue;
            }
            int nearCNC= findNearestCNC();
            int posOfNearCNC=nearCNC/2;
            System.out.println("最近CNC："+(nearCNC)+"  当前位置："+RGVposition);
            if (RGVposition==posOfNearCNC){
                //选择该位置上时间短的处理
                if (nearCNC%2==0){//同位置快CNC等待时间短
                    if (SignialQueue.get(nearCNC).addressTime>fastT){//该CNC还在加工，需执行等待操作
                        allWaitTime+=SignialQueue.get(nearCNC).addressTime-fastT;
                        update(SignialQueue.get(nearCNC).addressTime-fastT,-1);
                    }else {//上下料
                        int opCNCtype=SignialQueue.get(nearCNC).type;
                        if (opCNCtype==1){//还未上过料，只上料
                            SignialQueue.get(nearCNC).upLoad(CurrentTime,false);
//                            CurrentTime+=28;
                            update(fastT,-1);
                            continue;
                        }else if (opCNCtype==0){//下料，且downLoad方法已经有了上料过程
                            SignialQueue.get(nearCNC).downLoad(CurrentTime);
//                            CurrentTime+=28;
                            callClean=true;
                            update(fastT,-1);
                            continue;
                        }else {
                            System.out.println("程序出错");
                            break;
                        }
                    }
                }else {//同位置慢CNC等待时间短
                    if (SignialQueue.get(nearCNC).addressTime>slowT){//需执行等待操作
                        allWaitTime+=SignialQueue.get(nearCNC).addressTime-slowT;
                        update(SignialQueue.get(nearCNC).addressTime-slowT,-1);
                    }else {//上下料
                        int opCNCtype=SignialQueue.get(nearCNC).type;
                        if (opCNCtype==1){//还未上过料，只上料
                            SignialQueue.get(nearCNC).upLoad(CurrentTime,false);
//                            CurrentTime+=31;
                            update(slowT,-1);
                            continue;
                        }else if (opCNCtype==0){//下料，且downLoad方法已经有了上料过程
                            SignialQueue.get(nearCNC).downLoad(CurrentTime);
//                            CurrentTime+=31;
                            callClean=true;
                            update(slowT,-1);
                            continue;
                        }else {
                            System.out.println("程序出错");
                            break;
                        }
                    }
                }
            }else {//总和时间上位置与当前位置不一致，需执行移动操作
                int step=Math.abs(posOfNearCNC-RGVposition);
                int stepTime=0;
                if (step==1){
                    stepTime=stepT1;
                }else if (step==2){
                    stepTime=stepT2;
                }else if (step==3){
                    stepTime=stepT3;
                }else {
                    System.out.println("程序出错！");
                    break;
                }
                update(stepTime,posOfNearCNC);
            }
        }
        System.out.println("总共等待时间："+allWaitTime);
    }
    public static int findNearestCNC(){//返回时间最小的CNC
        int minTime=Integer.MAX_VALUE;
        int i;
        for (i=0;i<SignialQueue.size();++i){
            minTime=Math.min(minTime,SignialQueue.get(i).addressTime);
        }
        for (i=0;i<SignialQueue.size();++i){
            if (SignialQueue.get(i).addressTime==minTime){
                break;
            }
        }
        return i;
    }
    /**
     * 等待或移动操作发起
     * 2.moveToPos:移动操作需要填写0~3这些位置，非移动操作填-1表示该操作步不移动
     */
    public static void update(int stepTime,int moveToPos){
        System.out.println("==============CNC更新================");
        int preTime=CurrentTime;
        CurrentTime+=stepTime;
        if (moveToPos==-1){
            System.out.println("等待或因上下料清洗而刷新CNC状态："+stepTime+"秒");
        }else {
            System.out.println("从位置"+RGVposition+"移动到"+moveToPos+",用时："+stepTime+"秒");
            RGVposition=moveToPos;
        }
        for (int i=0;i<SignialQueue.size();++i){
            SignialQueue.get(i).update(preTime,CurrentTime,moveToPos==-1?RGVposition:moveToPos);//注意，该方法传入的是RGV完成该操作后的位置
        }
    }

    public static void main(String[] args) {
        initDatas();
        Assignment2(8*3600);
        for (int i=0;i<Downmaterials.size();++i){
            System.out.println("第"+i+"个物料数据如下：");
            System.out.println("所加工CNC编号："+Downmaterials.get(i).AssignedCNC);
            System.out.println("上料开始时间："+Downmaterials.get(i).upTime);
            System.out.println("下料开始时间："+Downmaterials.get(i).downTime);
            System.out.println("========================================================");
        }
        System.out.println("总共完成物料 "+Downmaterials.size()+"件");
        System.out.println("断点");
    }
}
