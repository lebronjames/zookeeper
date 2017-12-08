package com.zookeeper.demo;

import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * zookeeper 客户端 API测试
 * 
 * 项目名称: zookeeper 包: com.zookeeper.demo 类名称: ZkClient.java 类描述: 创建人: yzx 创建时间:
 * 2017年12月7日
 */
public class ZkClient {
	
	public static String fatherNode = "/heihei";

	public static ZooKeeper init() throws Exception {
		String connectionString = "10.5.2.241:2181,10.5.2.241:2182,10.5.2.241:2183";
		int sessionTimeout = 5000;
		ZooKeeper zookeeper = new ZooKeeper(connectionString, sessionTimeout, null);
		Thread.sleep(sessionTimeout);
		return zookeeper;
	}
	
	public static void closeConnection(ZooKeeper zookeeper) throws Exception {
		zookeeper.close();
	} 

	public static void main(String[] args) throws Exception {
		ZooKeeper zookeeper = init();
		createNode(zookeeper,fatherNode);
		String data = getNodeData(zookeeper,fatherNode);
		System.out.println("----------data:" + data);
		modifyData(zookeeper,fatherNode);
		String modifyData = getNodeData(zookeeper,fatherNode);
		System.out.println("----------modifyData:" + modifyData);
		createChildrenNode(zookeeper,fatherNode);
		getChildrenNode(zookeeper,fatherNode);
		String childData = getNodeData(zookeeper,fatherNode+"/child1");
		System.out.println("----------childData:" + childData);
		deleteNode(zookeeper,fatherNode+"/child1");
//		String deleteData = getNodeData(zookeeper,fatherNode+"/child1");
//		System.out.println("----------deleteData:" + deleteData);
		closeConnection(zookeeper);
	}

	//创建节点
	public static void createNode(ZooKeeper zookeeper,String path) throws Exception {
		zookeeper.create(path, "hahaxixi".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
	}

	//获取节点数据
	public static String getNodeData(ZooKeeper zookeeper,String path) throws Exception {
		Stat stat = new Stat();
		String data = new String(zookeeper.getData(path, true, stat));
		return data;
	}

	//更新节点数据
	public static void modifyData(ZooKeeper zookeeper,String path) throws Exception {
		zookeeper.setData(path, "doubi_modify".getBytes(), -1);// -1忽略版本号
	}

	//创建子节点
	public static void createChildrenNode(ZooKeeper zookeeper,String path) throws Exception {
		zookeeper.create(fatherNode+"/child1", "node-child1".getBytes(), 
				Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
	}
	
	//获取子节点,会在子点有变化时触发Watcher()这个监听器
	public static void getChildrenNode(ZooKeeper zookeeper,String path) throws Exception {
		List<String> children = zookeeper.getChildren(path, new Watcher() {
			public void process(WatchedEvent event) {
				System.out.println("this is children node event");
				System.out.println(event);
			}
		});
	}
	
	//删除节点
	public static void deleteNode(ZooKeeper zookeeper,String path) throws Exception {
		zookeeper.delete(path, -1);
	}
}
