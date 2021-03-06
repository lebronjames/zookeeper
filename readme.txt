zookeeper项目开发

一、下载tar包
wget https://mirrors.tuna.tsinghua.edu.cn/apache/zookeeper/zookeeper-3.4.10/zookeeper-3.4.10.tar.gz

二、解压
tar -zxvf zookeeper-3.4.10.tar.gz
 
三、移动到/usr/local路径下
mv zookeeper-3.4.10 zookeeper
mv zookeeper /usr/local

四、将安装完成的Zookeeper复制三份
cp -R zookeeper/ zookeeper1
cp -R zookeeper/ zookeeper2
cp -R zookeeper/ zookeeper3

五、三份分别创建日志、数据目录
cd /usr/local/zookeeper1
mkdir data
mkdir logs

六、分别为这3个Zookeeper在其对应的数据目录下创建服务器标识ID文件
/usr/local/zookeeper1/data/myid，其内容为：1，可通过如下命令创建：echo  1 > myid
/usr/local/zookeeper2/data/myid，其内容为：2，可通过如下命令创建：echo  2 > myid
/usr/local/zookeeper3/data/myid，其内容为：3，可通过如下命令创建：echo  3 > myid

七、分别进入/conf目录，拷贝一份zoo_sample.cfg，重命名为zoo.cfg
cd conf
cp zoo_sample.cfg zoo.cfg
 
八、修改zoo.cfg为： 
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/usr/local/zookeeper1/data/
dataLogDir=/usr/local/zookeeper1/logs/
clientPort=2181
maxClientCnxns=60
server.1=127.0.0.1:6660:7770
server.2=127.0.0.1:6661:7771
server.3=127.0.0.1:6662:7772

说明：  
dataDir：数据目录，不同的Zookeeper对应的目录不一样
dataLogDir：日志目录，不同的Zookeeper对应的目录不一样 
clientPort：客户端连接时用到的端口，不同的Zookeeper对应的目录不一样 
如果在1台机器上部署多个server，那么每台机器都要不同的 clientPort，比如 server1是2181,server2是2182，server3是2183
“server.1=127.0.0.1:6660:7770”表示集群中的服务器，其格式为： server.X=A:B:C，X是服务器标识ID，即其myid文件中的内容；A是该 Zookeeper的IP，C是该Zookeeper的IP端口；C是集群中各Zookeeper服务器间进行选举leader时要用到的端口。

九、分别，进入bin目录，启动
./zkServer.sh start

十、查看启动状态（可以查看leader,follower）
./zkServer.sh status

十一、使用命令操作
./zkCli.sh -server localhost:2181
ls /
cerate /zk myData
get /zk
set /zk hahah
delete /zk

十二、zookeeper java api
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

执行结果：
----------data:hahaxixi
----------modifyData:doubi_modify
----------childData:node-child1
this is children node event
WatchedEvent state:SyncConnected type:NodeChildrenChanged path:/heihei