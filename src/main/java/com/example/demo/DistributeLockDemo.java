package com.example.demo;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author shouhan
 * @version 1.0
 * @date 2018/06/25
 * @desc zooKeeper实现分布式锁
 * @see
 * @since 1.0
 */
public class DistributeLockDemo implements Watcher {

    //zookeeper原生api去实现一个分布式锁
    ZooKeeper zk = null;
    private String root = "/locks";
    //表示当前获取到的锁名称-也就是节点名称
    private String myZonode;
    // 表示当前等待的节点
    private String waitNode;
    private CountDownLatch latch;
    //zookeeper服务 url
    private static final String CONNECTION_STRING = "127.0.0.1:2180";
    //超时时间
    private static final int SESSION_TIMEOUT = 10000;

    /**
     * 构造函数
     *
     * @param config 表示zookeeper连接串
     */
    public DistributeLockDemo(String config) {
        try {
            zk = new ZooKeeper(config, SESSION_TIMEOUT, this);
            //判断是不是已经存在locks节点，不需要监听root节点
            Stat stat = zk.exists(root, false);
            //如果不存在，则创建根节点
            if (stat == null) {
                zk.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent event) {
        //如果计数器不为空话话，释放计数器锁
        if (this.latch != null) {
            this.latch.countDown();
        }
    }

    /**
     * 获取锁的方法
     */
    public boolean lock(String name) {
        if (tryLock(name)) {
             System.out.println(Thread.currentThread().getName() + "-->" + myZonode + " - 获取 lock!");
            return true;
        }
        try {
            return waitLock(waitNode, SESSION_TIMEOUT);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 释放锁操作的方法
     */
    public void unlock() {
        System.out.println(Thread.currentThread().getName() + "-->" + myZonode + "释放锁");
        try {
            zk.delete(myZonode, -1);
            myZonode = null;
            zk.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    private boolean tryLock(String name) {
        String splitStr = name; //lock_0000000001
        try {
            //创建一个有序的临时节点，赋值给myznode
            myZonode = zk.create(root + "/" + splitStr, new byte[0],
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println(Thread.currentThread().getName() + "-->" + myZonode + " 创建成功");
            List<String> subNodes = zk.getChildren(root, false);
            Collections.sort(subNodes); //讲所有的子节点排序
            if (myZonode.equals(root + "/" + subNodes.get(0))) {
                //当前客户端创建的临时有序节点是locks下节点中的最小的节点，表示当前的客户端能够获取到锁
                return true;
            }
            //否则的话,监听比自己小的节点 locks/lock_0000000003
            String subMyZnode = myZonode.substring((myZonode.lastIndexOf("/") + 1));
            waitNode = subNodes.get(Collections.binarySearch(subNodes, subMyZnode) - 1);// 获取比当前节点小的节点
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean waitLock(String lower, long waitTime) throws KeeperException, InterruptedException {
        Stat stat = zk.exists(root + "/" + lower, true); //获取节点状态，并添加监听
        if (stat != null) {
            System.out.println(Thread.currentThread().getName() + "-->" + myZonode + " waiting for" + root + " /" + lower);
            this.latch = new CountDownLatch(1); //实例化计数器，让当前的线程等待
            this.latch.await(waitTime, TimeUnit.MILLISECONDS);
            this.latch = null;
        }
        System.out.println("Thread " + myZonode + "获取锁");
        return true;
    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
//        final Semaphore semaphore = new Semaphore(10);
        DistributeLockDemo distributeLockDemo = new DistributeLockDemo(CONNECTION_STRING);
        boolean lock = distributeLockDemo.lock("product_");
        if (lock) {
            //业务代码
            System.out.println(Thread.currentThread().getName() + "-->" + "开始执行业务代码");
//                          Thread.sleep(3000);
            distributeLockDemo.unlock();
        }
    }

}
