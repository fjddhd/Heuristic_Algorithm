package graAA;

import ilog.concert.IloLQNumExpr;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.List;

public class expr {
    private static List<List<Integer>> LM;
    private static int[][] LMatrix;
    private static double[][] C;//m*n
    private static double[][] Q_cplex;//m*n
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
            initialData(8,15,2,5,6);
            cplexCode(N,M,K,Q_cplex,LMatrix,L,La,i);
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
        for (int i=0;i<K;++i){
            writeIntoMeM("D://实验数据.txt","\nLM: "+LM.get(i));
            System.out.println("LM: "+LM.get(i));
        }
        LMatrix=ArrayUtil.ListToArray2Dimension(LM);
        //根据LM统计出L和La
        L=new int [N];
        La=new int[M];
        for (int i=0;i<M;++i){
            for (int j=0;j<K;++j){
                La[i]+=LMatrix[j][i];
            }
        }
        writeIntoMeM("D://实验数据.txt","\nLa: "+ArrayUtil.arrayToList(La));
        System.out.println("\nLa: "+ArrayUtil.arrayToList(La));
        for (int i=M;i<M+N;++i){
            for (int j=0;j<K;++j){
                L[i-M]+=LMatrix[j][i];
            }
        }
        writeIntoMeM("D://实验数据.txt","\nL: "+ArrayUtil.arrayToList(L));
        System.out.println("\nL: "+ArrayUtil.arrayToList(L));

        //随机C矩阵
        C=new double[M][K];
        writeIntoMeM("D://实验数据.txt","\n自动生成C：");
        System.out.println("\n自动生成C：");
        StringBuilder sb_C=new StringBuilder();
        for (int i=0;i<m;++i){
            for (int j=0;j<K;++j){
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
//        System.out.println("断点");
    }
    public static void cplexCode(int n, int m,int k,double[][] Q, int[][] LMatrix, int[] L, int[] La,int term) {
        try {
            IloCplex cplex0 = new IloCplex();
            //目标函数和结果
            IloNumVar[][] T1 = new IloNumVar[m][n];
            int[][] T2 = new int[m][n];
            //约束1
            for (int i = 0; i < m; i++) {
                T1[i] = cplex0.numVarArray(n, 0, 1, IloNumVarType.Bool);
            }
            //目标函数
            IloLinearNumExpr exp0 = cplex0.linearNumExpr();
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    exp0.addTerm(Q[i][j], T1[i][j]);
                }
            }
            cplex0.addMaximize(exp0);
            //约束2
            for (int j = 0; j < n; j++) {
                IloLinearNumExpr availExpr = cplex0.linearNumExpr();
                for (int i = 0; i < m; i++) {
                    availExpr.addTerm(1, T1[i][j]);//addTerm(coef,var)
                }
                cplex0.addEq(availExpr, L[j]);
            }
            //约束3
            for (int i = 0; i < m; i++) {
                IloLinearNumExpr availExpr1 = cplex0.linearNumExpr();
                for (int j = 0; j < n; j++) {
                    availExpr1.addTerm(1, T1[i][j]);
                }
                cplex0.addLe(availExpr1, La[i]);//小于等于
            }
            //约束4
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    for (int v = 0; v < k; v++) {
                        IloLinearNumExpr availExpr1 = cplex0.linearNumExpr();
                        availExpr1.addTerm(1,T1[i][j]);
                        cplex0.addLe(availExpr1,LMatrix[v][i]*LMatrix[v][m+j]);
                    }
                }
            }
            //1
            if (cplex0.solve()) {
                cplex0.output().println("Solution status = " + cplex0.getStatus());
                cplex0.output().println("Solution value = " + cplex0.getObjValue());
                for (int i = 0; i < m; i++) {
                    for (int j = 0; j < n; j++) {
                        T2[i][j] = (int) cplex0.getValue(T1[i][j]);
                    }
                }
                countCplexSuccess++;
                System.out.println("第"+term+"次CPlex ：成功！");
                writeIntoMeM("D://实验数据.txt","第"+term+"次CPlex ：成功！");
                System.out.println("第"+term+"次CPlex结果：");
                writeIntoMeM("D://实验数据.txt","第"+term+"次CPlex结果：");
                for(int i=0;i<T2.length;++i){
                    System.out.println(ArrayUtil.arrayToList(T2[i]));
                    writeIntoMeM("D://实验数据.txt",ArrayUtil.arrayToList(T2[i]).toString());
                }
//                LCI[ci] = ci;
//                value = cplex0.getObjValue();

                cplex0.end();
                return;
            }
            writeIntoMeM("D://实验数据.txt","第"+term+"次CPlex ：失败！");
            System.out.println("第"+term+"次CPlex ：失败！");
            cplex0.end();//����
            //�������뱣������д��txt�ĵ�����1��CPLEX�н�Ĵ����ͳ������������(2)cplex�ͳ������1000�����н�ı�ţ�case�������һ���н⡣����
            //��3��cplex�� ��������ֱ����е�ʱ��
        } catch (Exception e) {
            System.err.println("Concert exception '" + e + "'caught");
        }
    }
    public static void writeIntoMeM(String path, String s){
        sb.append(s);
        sb.append("\n");
    }
}
