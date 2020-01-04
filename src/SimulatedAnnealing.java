/**
 * 模拟退火算法(Simulated Annealing, SA)的思想借鉴于固体的退火原理，
 * 当固体的温度很高的时候，内能比较大，固体的内部粒子处于快速无序运动，
 * 当温度慢慢降低的过程中，固体的内能减小，粒子的慢慢趋于有序，最终，
 * 当固体处于常温时，内能达到最小，此时，粒子最为稳定。模拟退火算法便是基于这样的原理设计而成。
 *
 * 1.初始化温度T（充分大），温度下限Tmin(充分小)，初始解X，每个T迭代次数为L；
 *
 * 2.随机生成临时解域X_new;
 *
 * 3.设f(x)函数来计算解的好坏，计算出f(X_new)-f(X);
 *
 * 4.如果f(X_new)-f(X)>0，说明新解比原来的解好，则无条件接受，如果f(X_new)-f(X)<0，则说明旧解比新解好，
 * 则以概率exp((f(X_new)-f(x))/k*T)接受X_new作为解。
 *
 * 5.如果当前温度小于Tmin的时候，退出循环，输出结果；否则，降低当前温度，T=a*T,(0<a<1)，跳转到第二步继续循环。
 *
 * 求解给定函数(6*x^7+8*x^6+7*x^3+5*x^2-x*y)的最小值：其中，0<=x<=100,给定任意y的值，求解x为多少的时候，F(x)最小？
 * **/
public class SimulatedAnnealing {
    public static final double T = 1000;// 初始化温度
    public static final double Tmin = 1;// 温度的下界
    public static final int k = 100;// 迭代的次数
    public static final double delta = 0.98;// 温度的下降率​
    public static double getX() {
        return Math.random() * 100;
    }
    /**
     * 评价函数的值,即对应上文中的f(x)
     *
     * @param x 目标函数中的一个参数
     * @param y 目标函数中的另一个参数
     * @return函数值
     */
    public static double getFuncResult(double x, double y) {
        double result=6*Math.pow(x,7)+8*Math.pow(x,6)+7*Math.pow(x,3)+5*Math.pow(x,2)-x*y;
        return result;
    }
    /**
     * 模拟退火算法的过程
     * @param y 目标函数中的指定的参数
     * @return最优解
     */
    public static double getSA(double y) {
        double t=T;
        double result = Double.MAX_VALUE;// 初始化最终的结果
        double x[] = new double[k];
        // 初始化初始解
        for (int i = 0; i < k; i++) {
            x[i] = getX();
        }
        // 迭代的过程
        while (t > Tmin) {
            for (int i = 0; i < k; i++) {
                // 计算此时的函数结果
                double funTmp = getFuncResult(x[i], y);
                // 在邻域内产生新的解
                double x_new = x[i] + (Math.random() * 2 - 1);
                // 判断新的x不能超出界
                if (x_new >= 0 && x_new <= 100) {
                    double funTmp_new = getFuncResult(x_new, y);
                    if (funTmp_new - funTmp < 0) {
                        // 替换
                        x[i] = x_new;
                    } else {
                        // 以概率替换
                        double p = 1 / (1 + Math
                                .exp(-(funTmp_new - funTmp) / T));
                        if (Math.random() < p) {
                            x[i] = x_new;
                        }
                    }
                }
            }
            t = t * delta;
        }
        for (int i = 0; i < k; i++) {
            result = Math.min(result, getFuncResult(x[i], y));
        }
        return result;
    }
    public static void main(String args[]) {
        // 设置y的值
        int y = 2;
        System.out.println("最优解为：" + getSA(y));
    }
}
