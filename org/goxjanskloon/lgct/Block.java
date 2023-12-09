package org.goxjanskloon.lgct;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
public class Block{
    public enum Type{
        Void(0),Or(1),Not(2),And(3),Xor(4),Src(5);
        private final int value;
        private Type(int argValue){value=argValue;}
        public static Type valueOf(int value){
            switch(value){
            case 0:return Void;
            case 1:return Or;
            case 2:return Not;
            case 3:return And;
            case 4:return Xor;
            case 5:return Src;
            default:return null;
            }
        }
        public int getValue(){return value;}
    }
    public AtomicInteger type;
    public AtomicBoolean value;
    public ArrayList<Pos> inPos,outPos;
    public Lock inPosLock=new ReentrantLock(),outPosLock=new ReentrantLock();
    public Block(){
        type=new AtomicInteger(0);
        value=new AtomicBoolean(false);
    }
    public Block(int initType,boolean initValue){
        type=new AtomicInteger(initType);
        value=new AtomicBoolean(initValue);
    }
}
