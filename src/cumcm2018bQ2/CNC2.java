package cumcm2018bQ2;

public class CNC2 {//0~7号

    public CNC2(int CNCnum, int RGVposition, int fastTime, int slowTime, int workTime) {
        this.CNCnum = CNCnum;
        this.CNCposition = CNCnum / 2;
        this.type = 1;
        this.udTime=(CNCnum%2==0?fastTime:slowTime);
        this.workTime=workTime;
        int step = Math.abs(this.CNCposition - RGVposition);
        if (step == 0) {
            this.addressTime = 0+udTime;
        } else if (step == 1) {
            this.addressTime = RGV2.stepT1+udTime;
        } else if (step == 2) {
            this.addressTime = RGV2.stepT2+udTime;
        } else if (step == 3) {
            this.addressTime = RGV2.stepT3+udTime;
        }else {
            System.err.println("cumcm2018bQ1.CNC2：步长计算出错,程序设计出错");
        }
    }
    public void update(int preTime,int newTime,int NewPos){//注意，该方法传入的是RGV完成该操作后的位置
        if (type==1 ||type==2){
            int step = Math.abs(this.CNCposition - NewPos);
            if (step == 0) {
                addressTime = 0+udTime;
            } else if (step == 1) {
                addressTime = RGV2.stepT1+udTime;
            } else if (step == 2) {
                addressTime = RGV2.stepT2+udTime;
            } else if (step == 3) {
                addressTime = RGV2.stepT3+udTime;
            }else {
                System.err.println("cumcm2018bQ1.CNC2：步长计算出错,程序设计出错");
            }
            System.out.println(CNCnum+" 号CNC未上过料，剩余时间为"+addressTime);
        }else {//type==0,加工（或清洗）中
            if (addressTime-(newTime-preTime)>udTime){//当前处理时间减去该操作更新时间大于上下料时间，表示还未加工完
                addressTime-=(newTime-preTime);
                System.out.println(CNCnum+" 号CNC正在加工中，减去上步时间："+(newTime-preTime)+",剩余时间为"+addressTime);
            }else {//加工完了，按照移动距离耗时和下料时间计时，以下算是代替了原设定的状态2：下料等待的状态刷新
                int step = Math.abs(this.CNCposition - NewPos);
                if (step == 0) {
                    addressTime = 0+udTime;
                } else if (step == 1) {
                    addressTime = RGV2.stepT1+udTime;
                } else if (step == 2) {
                    addressTime = RGV2.stepT2+udTime;
                } else if (step == 3) {
                    addressTime = RGV2.stepT3+udTime;
                }else {
                    System.err.println("cumcm2018bQ1.CNC2：步长计算出错,程序设计出错");
                }
                System.out.println(CNCnum+" 号CNC正在加工完成，剩余时间为"+addressTime);
//                type=2;
            }
        }
    }
    public void upLoad(int newTime,Boolean isNeedClean){//传入时间为该操作刚开始时间
        System.out.println("第"+CNCnum+"号CNC正在上料");
        mm=new MyMaterial2(newTime,CNCnum);
        RGV2.Upmaterials.add(mm);
        type=0;
        addressTime=workTime+2*udTime;//RGV在当前位置
    }
    public void downLoad(int newTime){//传入时间为该操作刚开始时间
        System.out.println("第"+CNCnum+"号CNC正在下料");
        mm.downTime=newTime;//下料开始时间
        if (mm.downTime+udTime+25<=8*3600) {
            RGV2.Upmaterials.remove(mm);
            RGV2.Downmaterials.add(mm);
        }
        upLoad(newTime,true);
    }
    public void clean(){//指清洗由该cnc刚下来的物料
    }

    int CNCnum;
    int CNCposition;
    int type;//0,1,2。0表示加工中，addressTime为当前位置到该cnc位置移动时间+加工剩余时间+上（下）料时间；
    // 1,2表示上下料，addressTime当前位置到该cnc位置移动时间+上（下）料时间
    //1仅在从未上过料时存在，因为之后的上料都是在下料后马上上料,type一直保持为0
    int addressTime;//距离最快上料完成或距离最快下料完成时间
    int udTime;
    int workTime;
    MyMaterial2 mm;
}
