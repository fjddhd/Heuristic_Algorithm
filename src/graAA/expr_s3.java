package graAA;

import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.List;

public class expr_s3 {
    private static List<List<Integer>> LM;
    private static int[][] Ap;
    private static int[][] Rp;
    private static int[][] Ap_star;
    private static int[][] Rp_star;
    private static int[][][] D;//m*n*k
    private static double[][] C;//K*m
    private static double[][][] Q_cplex;//m*n
    private static int[] L;
    private static int[] La;
    private static int N;
    private static int M;
    private static int K;
    private static int countSuccess=0;
    private static int countCplexSuccess=0;


    private static StringBuilder sb;

    public static void main(String[] args) {
        sb=new StringBuilder();
        countSuccess=0;
        countCplexSuccess=0;
        int i;
        for (i=1;i<=100;++i) {
            initialData(8,15,3,5,6);
            cplexCode(i);
        }
        writeIntoMeM("D://实验数据.txt","CPlex共测试"+(i-1)+"次，CPlex共测试成功次数为："+countCplexSuccess);
        System.out.println("CPlex共测试"+(i-1)+"次，CPlex共测试成功次数为："+countCplexSuccess);
    }

    public static void initialData(int n,int m,int k,int maxLa,int maxL){
        N=n;M=m;K=k;
//        int maxLa= (int) (m*m*maxLaRate);
//        int minLa= (int) (m*m*lowLaRate);
//        int maxL= (int) (n*n*maxLRate);
//        int minL= (int) (n*n*lowLRate);
        writeIntoMeM("D://实验数据.txt","======================================================");
        System.out.println("======================================================");
        writeIntoMeM("D://实验数据.txt","N= "+n+" , "+"M= "+m+" , "+"maxLa= "+maxLa+",maxL="+maxL);
        System.out.println("N= "+n+" , "+"M= "+m+" , "+"maxLa= "+maxLa+",maxL="+maxL);
        //随机LMatrix
        LM=new ArrayList<>();
        for (int i=0;i<K;++i){
            List<Integer> tempList=new ArrayList<>();
            for (int j=0;j<M+N;++j){
                tempList.add(0);
            }
            LM.add(tempList);
        }
        for (int i=0;i<M;++i){
            int La_i= (int) (Math.random()*maxLa)+1;
            for (int j=0;j<La_i;++j){
                int v= (int) (Math.random()*K);
                LM.get((int)(v)).set(i,LM.get((int)(v)).get(i)+1);
            }
        }
        for (int i=M;i<M+N;++i){
            int La_i= (int) (Math.random()*maxL)+1;
            for (int j=0;j<La_i;++j){
                int v= (int) (Math.random()*K);
                LM.get((int)(v)).set(i,LM.get((int)(v)).get(i)+1);
            }
        }
        //根据LM生成Ap、Rp、Ap_star、Rp_star
        Ap=new int[K][M];Rp=new int[K][N];Ap_star=new int[K][M];Rp_star=new int[K][N];
        for (int i=0;i<K;++i){
            for (int j=0;j<M+N;++j){
                if (j<M){
                    Ap[i][j]=LM.get(i).get(j);
                    Ap_star[i][j]=(LM.get(i).get(j)==0?0:1);
                }else {
                    Rp[i][j-M]=LM.get(i).get(j);
                    Rp_star[i][j-M]=(LM.get(i).get(j)==0?0:1);
                }
            }
        }
        for (int i=0;i<K;++i) {
            writeIntoMeM("D://实验数据.txt","\nAp "+ArrayUtil.arrayToList(Ap[i]));
            System.out.println("Ap第"+(i+1)+"行: "+ArrayUtil.arrayToList(Ap[i]));
        }
        for (int i=0;i<K;++i) {
            writeIntoMeM("D://实验数据.txt","\nRp "+ArrayUtil.arrayToList(Rp[i]));
            System.out.println("Rp第"+(i+1)+"行: "+ArrayUtil.arrayToList(Rp[i]));
        }
        //根据Ap,Rp统计出L和La
        L=new int [N];
        La=new int[M];
        for (int i=0;i<M;++i){
            for (int j=0;j<K;++j){
                La[i]+=Ap[j][i];
            }
        }
        writeIntoMeM("D://实验数据.txt","\nLa: "+ArrayUtil.arrayToList(La));
        System.out.println("\nLa: "+ArrayUtil.arrayToList(La));
        for (int i=0;i<N;++i){
            for (int j=0;j<K;++j){
                L[i]+=Rp[j][i];
            }
        }
        writeIntoMeM("D://实验数据.txt","\nL: "+ArrayUtil.arrayToList(L));
        System.out.println("\nL: "+ArrayUtil.arrayToList(L));
        //计算D矩阵
        writeIntoMeM("D://实验数据.txt","\nD矩阵: ");
        System.out.println("\nD矩阵: ");
        D=new int[M][N][K];
        for (int i=0;i<M;++i){
            for (int j=0;j<N;++j){
                for (int v=0;v<K;++v){
                    D[i][j][v]=Math.min(Ap[v][i],Rp[v][j]);
                }
            }
        }
        int[][][] D_print=new int[K][M][N];
        for (int v=0;v<K;++v) {
            for (int i = 0; i < M; i++) {
                for (int j = 0; j < N; j++) {
                    D_print[v][i][j] = D[i][j][v];
                }
            }
        }
        for (int v=0;v<K;++v){
            writeIntoMeM(" ","D,v="+v+"：");
            System.out.println("D,v="+v+"：");
            for (int i=0;i<M;++i){
                writeIntoMeM(" ",ArrayUtil.arrayToList(D_print[v][i]).toString());
                System.out.println(ArrayUtil.arrayToList(D_print[v][i]).toString());
            }
        }
        //随机C矩阵
        C=new double[K][M];
        writeIntoMeM("D://实验数据.txt","\n自动生成C：");
        System.out.println("\n自动生成C：");
        StringBuilder sb_C=new StringBuilder();
        for (int i=0;i<K;++i){
            for (int j=0;j<M;++j){
                double temp=Math.random();
                temp = (double) Math.round(temp * 100) / 100;
                C[i][j]=temp;
                sb_C.append(temp+",");
                System.out.print(temp+",");
            }
            sb_C.append("\n");
            System.out.println();
        }
        writeIntoMeM("D://实验数据.txt",sb_C.toString());
        //评估Q矩阵
        Q_cplex=new double[M][N][K];
        writeIntoMeM("D://实验数据.txt","\n评估Q矩阵：");
        System.out.println("\n评估Q矩阵：");
        for (int i=0;i<M;++i){
            for (int j=0;j<N;++j){
                for (int v=0;v<K;++v){
                    Q_cplex[i][j][v]=Ap_star[v][i]*Rp_star[v][j]==0?0:1-C[v][i];
                    Q_cplex[i][j][v] = (double) Math.round(Q_cplex[i][j][v] * 100) / 100;
//                    writeIntoMeM(" ","Q_cplex[i="+i+",j="+j+",v="+v+"]:"+Q_cplex[i][j][v]+"\n");
//                    System.out.println("Q_cplex[i="+i+",j="+j+",v="+v+"]:"+Q_cplex[i][j][v]);
                }
            }
        }
        double[][][] Q_print=new double[K][M][N];
        for (int v=0;v<K;++v) {
            for (int i = 0; i < M; i++) {
                for (int j = 0; j < N; j++) {
                    Q_print[v][i][j] = Q_cplex[i][j][v];
                }
            }
        }
        for (int v=0;v<K;++v){
            writeIntoMeM(" ","Q'矩阵,v="+v+"：");
            System.out.println("Q'矩阵,v="+v+"：");
            for (int i=0;i<M;++i){
                writeIntoMeM(" ",ArrayUtil.arrayToListDouble(Q_print[v][i]).toString());
                System.out.println(ArrayUtil.arrayToListDouble(Q_print[v][i]).toString());
            }
        }
//        System.out.println("断点");
    }
    public static void cplexCode(int term) {
        try {
            IloCplex cplex0 = new IloCplex();
            //目标函数和结果
            IloNumVar[][][] T1 = new IloNumVar[M][N][K];
            int[][][] T2 = new int[K][M][N];//K*M*N 方便打印
            //约束1
            for (int i = 0; i < M; i++) {
                for (int j=0;j<N;++j){
                    for (int v=0;v<K;++v){
                        T1[i][j][v] = cplex0.numVar(0, D[i][j][v], IloNumVarType.Int);
                    }
                }
//                T1[i] = cplex0.numVarArray(n, 0, 1, IloNumVarType.Bool);
            }
//            for (int i = 0; i < m; i++) {
//                for (int j=0;j<n;++j){
//                    T1[i][j] = cplex0.numVar(0, L[j], IloNumVarType.Int);//
//                }
//            }
            //目标函数
            IloLinearNumExpr exp0 = cplex0.linearNumExpr();
            for (int i = 0; i < M; i++) {
                for (int j=0;j<N;++j){
                    for (int v=0;v<K;++v){
                        exp0.addTerm(Q_cplex[i][j][v], T1[i][j][v]);
                    }
                }
//                T1[i] = cplex0.numVarArray(n, 0, 1, IloNumVarType.Bool);
            }
            cplex0.addMaximize(exp0);
            //约束2
            for (int v=0;v<K;++v) {
                for (int j = 0; j < N; j++) {
                    IloLinearNumExpr availExpr = cplex0.linearNumExpr();
                    for (int i = 0; i < M; i++) {
                        availExpr.addTerm(1, T1[i][j][v]);//addTerm(coef,var)
                    }
                    cplex0.addEq(availExpr, Rp[v][j]);
                }
            }
            //约束3
            for (int v=0;v<K;++v) {
                for (int i = 0; i < M; i++) {
                    IloLinearNumExpr availExpr = cplex0.linearNumExpr();
                    for (int j = 0; j < N; j++) {
                        availExpr.addTerm(1, T1[i][j][v]);//addTerm(coef,var)
                    }
                    cplex0.addLe(availExpr, Ap[v][i]);
                }
            }
            //如果成功
            if (cplex0.solve()) {
                cplex0.output().println("Solution status = " + cplex0.getStatus());
                cplex0.output().println("Solution value = " + cplex0.getObjValue());
                for (int v=0;v<K;++v) {
                    for (int i = 0; i < M; i++) {
                        for (int j = 0; j < N; j++) {
                            T2[v][i][j] = (int) cplex0.getValue(T1[i][j][v]);
                        }
                    }
                }
                countCplexSuccess++;
                System.out.println("第"+term+"次CPlex ：成功！");
                writeIntoMeM("D://实验数据.txt","第"+term+"次CPlex ：成功！");
                System.out.println("第"+term+"次CPlex结果：");
                writeIntoMeM("D://实验数据.txt","第"+term+"次CPlex结果：");
                for(int v=0;v<T2.length;++v){
                    System.out.println("v="+v+"时：");
                    for (int i=0;i<M;++i) {
                        System.out.println(ArrayUtil.arrayToList(T2[v][i]));
                        writeIntoMeM("D://实验数据.txt",ArrayUtil.arrayToList(T2[v][i]).toString());
                    }
                }
//                LCI[ci] = ci;
//                value = cplex0.getObjValue();

                cplex0.end();
                return;
            }
            writeIntoMeM("D://实验数据.txt","第"+term+"次CPlex ：失败！");
            System.out.println("第"+term+"次CPlex ：失败！");
            cplex0.end();

        } catch (Exception e) {
            System.err.println("Concert exception '" + e + "'caught");
        }
    }
    public static void writeIntoMeM(String path, String s){
        sb.append(s);
        sb.append("\n");
    }
}
