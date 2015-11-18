package com.example.autoio_navi_bt;
/*
 * author autoio jone 2015 1103
 * 百度导航sdk，蓝牙传输，创建进sd卡文件
 * 
 * */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.baidu.navisdk.remote.BNRemoteVistor;
import com.baidu.navisdk.remote.aidl.BNEventListener;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class NavActivity extends Activity implements OnItemClickListener{
	private final static String TAG = "jone_test";
	private ListView lvDevices;
	private BluetoothAdapter bluetoothAdapter;
	private List<String> bluetoothDevices = new ArrayList<String>();
	private ArrayAdapter<String> arrayAdapter;
	private AcceptThread acceptThread;
	private BluetoothSocket clientSocket;
	private BluetoothDevice device;
	private OutputStream os;
	private static String SDPATH; 

	 /** 
     *  
     * 定义了机动点显示图标的对应关系，如maneuverName 为R.drawable.nsdk_drawable_rg_ic_turn_back 的 turn_back字段，具体图标请用户自行设计。 
     */  
	public static final String[] gTurnIconID = {  
		  "turn_back"  ,               /**<  无效值 0*/  
		  "turn_front"  ,                  /**<  直行 1*/  
		  "turn_right_front"  ,        /**<  右前方转弯 2*/  
		  "turn_right"  ,                  /**<  右转 3*/  
		  "turn_right_back"  ,             /**<  右后方转弯 4*/  
		  "turn_back"  ,               /**<  掉头 5*/  
		  "turn_left_back"  ,              /**<  左后方转弯 6*/  
		  "turn_left"  ,               /**<  左转 7*/  
		  "turn_left_front"  ,             /**<  左前方转弯 8*/  
		  "turn_ring"  ,               /**<  环岛 9*/  
		  "turn_ring_out"  ,           /**<  环岛出口 10*/  
		  "turn_left_side"  ,              /**<  普通/JCT/SAPA二分歧 靠左 11*/  
		  "turn_right_side"  ,             /**<  普通/JCT/SAPA二分歧 靠右 12*/  
		  "turn_left_side_main"  ,         /**<  左侧走本线 13*/  
		  "turn_branch_left_straight"  , /**<  靠最左走本线 14*/  
		  "turn_right_side_main"  ,    /**<  右侧走本线 15*/  
		  "turn_branch_right_straight" , /**<  靠最右走本线 16*/  
		  "turn_branch_center"  ,        /**<  中间走本线 17*/  
		  "turn_left_side_ic"  ,       /**<  IC二分歧左侧走IC 18*/  
		  "turn_right_side_ic"  ,          /**<  IC二分歧右侧走IC 19*/  
		  "turn_branch_left"  ,        /**<  普通三分歧/JCT/SAPA 靠最左 20*/  
		  "turn_branch_right"  ,       /**<  普通三分歧/JCT/SAPA 靠最右 21*/  
		  "turn_branch_center"  ,          /**<  普通三分歧/JCT/SAPA 靠中间 22*/  
		  "turn_start"  ,                  /**<  起始地 23*/  
		  "turn_dest"  ,               /**<  目的地 24*/  
		  "turn_via_1" ,                /**<  途径点1 25*/  
		  "turn_via_1" ,                /**<  途径点2 26*/  
		  "turn_via_1" ,                /**<  途径点3 27*/  
		  "turn_via_1" ,                /**<  途径点4 28*/  
		  "turn_inferry"  ,            /**<  进入渡口 29*/  
		  "turn_inferry" ,             /**<  脱出渡口 30*/  
		  "turn_tollgate"  ,             /**<  收费站 31*/  
		  "turn_left_side_main"  ,       /**<  IC二分歧左侧直行走IC 32*/  
		  "turn_right_side_main"  ,      /**<  IC二分歧右侧直行走IC 33*/  
		  "turn_left_side_main"  ,       /**<  普通/JCT/SAPA二分歧左侧 直行 34*/  
		  "turn_right_side_main"  ,      /**<  普通/JCT/SAPA二分歧右侧 直行 35*/  
		  "turn_branch_left_straight"  , /**<  普通/JCT/SAPA三分歧左侧 直行 36*/  
		  "turn_branch_center"  ,        /**<  普通/JCT/SAPA三分歧中央 直行 37*/  
		  "turn_branch_right_straight"  ,/**<  普通/JCT/SAPA三分歧右侧 直行 38*/  
		  "turn_branch_left"  ,          /**<  IC三分歧左侧走IC 39*/  
		  "turn_branch_center"  ,        /**<  IC三分歧中央走IC 40*/  
		  "turn_branch_right"  ,         /**<  IC三分歧右侧走IC 41*/  
		  "turn_branch_left_straight"  , /**<  IC三分歧左侧直行 42*/  
		  "turn_branch_center"  ,          /**<  IC三分歧中间直行 43*/  
		  "turn_branch_right_straight" ,   /**<  IC三分歧右侧直行 44*/  
		  "turn_left_side_main" ,  /**<  八方向靠左直行 45*/  
		  "turn_right_side_main" ,  /**<  八方向靠右直行 46*/  
		  "turn_branch_left_straight" , /**<  八方向靠最左侧直行 47*/  
		  "turn_branch_center" ,  /**<  八方向沿中间直行 48*/  
		  "turn_branch_right_straight" ,  /**<  八方向靠最右侧直行 49*/  
		  "turn_left_2branch_left" ,  /**<  八方向左转+随后靠左 50*/  
		  "turn_left_2branch_right" ,  /**<  八方向左转+随后靠右 51*/  
		  "turn_left_3branch_left" ,  /**<  八方向左转+随后靠最左 52*/  
		  "turn_left_3branch_middle" ,/**<  八方向左转+随后沿中间 53*/  
		  "turn_left_3branch_right" ,  /**<  八方向左转+随后靠最右 54*/  
		  "turn_right_2branch_left" , /**<  八方向右转+随后靠左 55*/  
		  "turn_right_2branch_right" ,  /**<  八方向右转+随后靠右 56*/  
		  "turn_right_3branch_left" , /**<  八方向右转+随后靠最左 57*/  
		  "turn_right_3branch_middle" , /**<  八方向右转+随后沿中间 58*/  
		  "turn_right_3branch_right" ,  /**<  八方向右转+随后靠最右 59*/  
		  "turn_lf_2branch_left" ,    /**<  八方向左前方靠左侧 60*/  
		  "turn_lf_2branch_right" ,    /**<  八方向左前方靠右侧 61*/    
		  "turn_rf_2branch_left" ,     /**<  八方向右前方靠左侧 62*/  
		  "turn_rf_2branch_right"     /**<  八方向右前方靠右侧 63*/  
    };  

	private SharedPreferences mySharedPreferences;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_nav);
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		SDPATH = Environment.getExternalStorageDirectory().getPath(); 
	    Log.e(TAG, "onCreate: SDPATH = "+SDPATH);
	    try {
			createSDFile("autoioNavi.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//实例化SharedPreferences对象（第一步） 
				mySharedPreferences= getSharedPreferences("test", 
				Activity.MODE_PRIVATE); 
				bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

				lvDevices = (ListView) findViewById(R.id.lvDevices);

				Set<BluetoothDevice> pairedDevices = bluetoothAdapter
						.getBondedDevices();

				if (pairedDevices.size() > 0)
				{
					for (BluetoothDevice device : pairedDevices)
					{
						bluetoothDevices.add(device.getName() + ":"
								+ device.getAddress() + "\n");
					}
				}
				arrayAdapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, android.R.id.text1,
						bluetoothDevices);

				lvDevices.setAdapter(arrayAdapter);
				lvDevices.setOnItemClickListener(this);
				IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
				this.registerReceiver(receiver, filter);

				filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
				this.registerReceiver(receiver, filter);
				
		 BNRemoteVistor.getInstance().setOnConnectListener(mOncConnectListener);
	     BNRemoteVistor.getInstance().connectToBNService(getApplicationContext());
	     acceptThread = new AcceptThread();
		 acceptThread.start();
	}
	 private BNEventListener.Stub mBNEventListener = new BNEventListener.Stub() {

	        /**
	         * 辅助诱导图标更新回调
	         * 
	         * @param assitantType 辅助诱导类型 {@link com.baidu.navisdk.remote.BNRemoteConstants.AssitantType
	         *            <code>AssitantType</code>}
	         * @param limitedSpeed 当类型是SpeedCamera和IntervalCamera的时候，会带有限速的值
	         * @param distance 诱导距离(以米位单位),当距离为0时，表明这个诱导丢失消失
	         */
	        @Override
	        public void onAssistantChanged(final int assistantType, final int limitedSpeed,final int distance) throws RemoteException {
	            Log.e(TAG, "onRoadCameraChanged: assistantType = " + assistantType + " ,limitedSpeed = " + limitedSpeed + " ,distance = " + distance);
	           
	                }

	        /**
	         * GPS速度变化，在实际应用中，在某些情况下，手机GPS速度与实际车速在相差4km/h左右
	         * 
	         * @param speed gps速度，单位km/h
	         * @param latitude 纬度，GCJ-02坐标
	         * @param longitude 经度，GCJ-02坐标
	         */
	        @Override
	        public void onGpsChanged(final int speed,final double latitude,final double longitude) {
	            Log.e(TAG, "onGpsSpeedChanged: speed = " + speed + " ,latitude = " + latitude + " ,longitude = " + longitude);
	           
	                }

	        /**
	         * 服务区更新回调
	         * 
	         * @param serviceArea 服务区的名字
	         * @param distance 服务区的距离，当distance为0或者serviceArea为空时，表明服务区消失
	         */
	        @Override
	        public void onServiceAreaChanged(final String serviceArea,final int distance) {
	            Log.e(TAG, "onServiceAreaChanged: serviceArea = " + serviceArea + " ,distance = " + distance);
	           
	                }

	        /** 
	         * 导航机动点更新 
	         *  
	         * @param maneuverName 下一个机动点名称，具体可以参考gTurnIconID数组的定义，每一个机动点名称对应的图标 
	         * @param distance 距离下一个机动点距离（以米为单位） 
	         */ 
	        @Override
	        public void onManeuverChanged(final String maneuverName,final int distance) {
	            Log.e(TAG, "onManeuverChanged: maneuverName = " + maneuverName + " ,distance = " + distance);
	            writeSDFile("qian mian "+distance+"M "+maneuverName + "----"+ getTurnStringId(maneuverName,gTurnIconID),"autoioNavi.txt");
	            runOnUiThread(new Runnable() {

	                @Override
	                public void run() {
	            Toast.makeText(getApplicationContext(), "前方 "+distance+" 米 "+maneuverName+ "----"+ getTurnStringId(maneuverName,gTurnIconID), Toast.LENGTH_LONG).show();
	                }
	            });
	            
	            String name_adress =mySharedPreferences.getString("address_falg", "no"); 
	            sendToAdress(name_adress,"前方 "+distance+" 米 "+maneuverName + "----"+ getTurnStringId(maneuverName,gTurnIconID));
	                }

	        /**
	         * 到达目的地的距离和时间更新
	         * 
	         * @param remainDistance 到达目的地的剩余距离（以米为单位）
	         * @param remainTime 到达目的地的剩余时间(以秒为单位)
	         */
	        @Override
	        public void onRemainInfoChanged(final int remainDistance,final int remainTime) {
	            Log.e(TAG, "onRemainInfoChanged: remainDistance = " + remainDistance + " ,remainTime = " + remainTime);
	            
	                }

	        /**
	         * 当前道路名更新
	         * 
	         * @param currentRoadName 当前路名
	         */
	        @Override
	        public void onCurrentRoadNameChanged(final String currentRoadName) {
	            Log.e(TAG, "onCurrentRoadNameChanged: currentRoadName = " + currentRoadName);
	           
	                }

	        /**
	         * 下一道路名更新
	         * 
	         * @param nextRoadName 下一个道路名
	         */
	        @Override
	        public void onNextRoadNameChanged(final String nextRoadName) {
	            Log.e(TAG, "onNextRoadNameChanged: nextRoadName = " + nextRoadName);
	           
	                }

	        /**
	         * 开始导航
	         */
	        @Override
	        public void onNaviStart() {
	            Log.e(TAG, "onNaviStart");
	            String name_adress =mySharedPreferences.getString("address_falg", "no"); 
	            writeSDFile("onNaviStart","autoioNavi.txt");
	            runOnUiThread(new Runnable() {

	                @Override
	                public void run() {
	            Toast.makeText(getApplicationContext(), "onNaviStart", Toast.LENGTH_LONG).show();
	                }
	            });
	            sendToAdress(name_adress,"开始导航！"); 
	                    
	                }

	        /**
	         * 结束导航
	         */
	        @Override
	        public void onNaviEnd() {
	            Log.e(TAG, "onNaviEnd");
	            Log.e(TAG, "onNaviEnd---int=="+ getTurnStringId("turn_left_front",gTurnIconID)); 
	            String name_adress =mySharedPreferences.getString("address_falg", "no"); 
	            writeSDFile("onNaviEnd","autoioNavi.txt");
	            runOnUiThread(new Runnable() {

	                @Override
	                public void run() {
	            Toast.makeText(getApplicationContext(), "onNaviEnd", Toast.LENGTH_LONG).show();
	                }
	            });
	            sendToAdress(name_adress,"结束导航！");
	                }

	        /**
	         * 电子狗模式开启
	         */
	        @Override
	        public void onCruiseStart() {
	            Log.e(TAG, "onCruiseStart");
	           
	          
	                }

	        /**
	         * 电子狗模式结束
	         */
	        @Override
	        public void onCruiseEnd() {
	            Log.e(TAG, "onCruiseEnd");
	            
	                }

	        /**
	         * 导航中偏航
	         */
	        @Override
	        public void onRoutePlanYawing() {
	            Log.e(TAG, "onRoutePlanYawing");
	            
	                }

	        /**
	         * 导航中偏航结束,重新算路成功
	         */
	        @Override
	        public void onReRoutePlanComplete() {
	            Log.e(TAG, "onReRoutePlanComplete");
	           
	                }

	        /**
	         * gps丢失
	         */
	        @Override
	        public void onGPSLost() {
	            Log.e(TAG, "onGPSLost");
	            String name_adress =mySharedPreferences.getString("address_falg", "no"); 
	            sendToAdress(name_adress,"gps丢失！");
	                }

	        /**
	         * gps正常
	         */
	        @Override
	        public void onGPSNormal() {
	            Log.e(TAG, "onGPSNormal");
	            String name_adress =mySharedPreferences.getString("address_falg", "no"); 
	            sendToAdress(name_adress,"gps正常！");
	                }

	        /**
	         * 扩展事件接口，现在暂时没有数据回调，用作以后扩展
	         * 
	         * @param eventType
	         * @param data
	         */
	        @Override
	        public void onExtendEvent(final int eventType,final Bundle data) {
	            Log.e(TAG, "onExtendEvent: eventType = " + eventType + " ,data = " + data.toString());
	            runOnUiThread(new Runnable() {

	                @Override
	                public void run() {
	            Toast.makeText(getApplicationContext(),"onExtendEvent: eventType = " + eventType + " ,data = " + data.toString() , Toast.LENGTH_LONG).show();
	                }
	            });
	                }

	    };
	    
	 private BNRemoteVistor.OnConnectListener mOncConnectListener = new BNRemoteVistor.OnConnectListener() {

	        @Override
	        public void onDisconnect() {
	            // TODO Auto-generated method stub

	        }

	        @Override
	        public void onConnectSuccess() {
	            // TODO Auto-generated method stub
	            try {
	                BNRemoteVistor.getInstance().setBNEventListener(mBNEventListener);
	            } catch (RemoteException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }
	            runOnUiThread(new Runnable() {

	                @Override
	                public void run() {
	                    Toast.makeText(getApplicationContext(), "connect success", Toast.LENGTH_LONG).show();
	                }
	            });
	        }

	        @Override
	        public void onConnectFail(final String reason) {
	            // TODO Auto-generated method stub
	            runOnUiThread(new Runnable() {

	                @Override
	                public void run() {
	                    Toast.makeText(getApplicationContext(), "connect fail reason:" + reason, Toast.LENGTH_LONG).show();
	                }
	            });
	        }
	    };
	    
	@Override
    public void onDestroy() {
        BNRemoteVistor.getInstance().disconnectToBNService(getApplicationContext());
        super.onDestroy();
    }
/*
 * jone 定位字串在数组中的位置
 * String turn 字串
 * String[] s 数组
 * 
 * */
	public int getTurnStringId(String turn,String[] s){
		int index = -1;
		for (int i=0;i<s.length;i++) {
		    if (s[i].equals(turn)) {
		        index = i;
		        break;
		    }
		}
		return index;		
	}
	public void onClick_Search(View view)
	{
		setProgressBarIndeterminateVisibility(true);
		setTitle("正在扫描...");

		if (bluetoothAdapter.isDiscovering())
		{
			bluetoothAdapter.cancelDiscovery();
		}
		bluetoothAdapter.startDiscovery();
	}
	
	public void onClick_sed(View view)
	{
		String name_adress =mySharedPreferences.getString("address_falg", "no"); 
		Log.d("jone_test","name_adress=="+name_adress);
		if(!name_adress.equals("no")){
			sendToAdress(name_adress,"发送好了，收到了吗！");
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id)
	{
		String s = arrayAdapter.getItem(position);
		String address = s.substring(s.indexOf(":") + 1).trim();
		//实例化SharedPreferences.Editor对象（第二步） 
		SharedPreferences.Editor editor = mySharedPreferences.edit(); 
		//用putString的方法保存数据 
		editor.putString("address_falg", address); 	
		//提交当前数据 
		editor.commit(); 
		sendToAdress(address,"已经配对好！");

	}

	private void sendToAdress(String adress,String sendmessage)
		{
		try
		{
			if (bluetoothAdapter.isDiscovering())
			{
				this.bluetoothAdapter.cancelDiscovery();
			}

			try
			{

				if (device == null)
				{
					device = bluetoothAdapter.getRemoteDevice(adress);
				}

				if (clientSocket == null)
				{
					//clientSocket = device.createRfcommSocketToServiceRecord(MY_UUID);

					Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
					clientSocket = (BluetoothSocket) m.invoke(device, 29);
					
					 if (device.getBondState() == BluetoothDevice.BOND_NONE) {  
			                Method creMethod = BluetoothDevice.class.getMethod("createBond");  
			                Log.e("TAG", "开始配对");  
			                creMethod.invoke(device);  
			          } 
					clientSocket.connect();
					os = clientSocket.getOutputStream();
				}

			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			if (os != null)
			{

				os.write(sendmessage.getBytes("utf-8"));

				Toast.makeText(this, "信息发送成功.", Toast.LENGTH_LONG).show();
			}
			else
			{
				Toast.makeText(this, "信息发送失败.", Toast.LENGTH_LONG).show();
			}
		}
		catch (Exception e)
		{
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();

		}
		}
	private final BroadcastReceiver receiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();

			if (BluetoothDevice.ACTION_FOUND.equals(action))
			{

				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				if (device.getBondState() != BluetoothDevice.BOND_BONDED)
				{
					bluetoothDevices.add(device.getName() + ":"
							+ device.getAddress() + "\n");
					arrayAdapter.notifyDataSetChanged();
				}

			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
			{
				setProgressBarIndeterminateVisibility(false);
				setTitle("连接蓝牙设备");

			}
		}
	};
	private Handler handler = new Handler()
	{

		@Override
		public void handleMessage(Message msg)
		{
			Toast.makeText(NavActivity.this, String.valueOf(msg.obj),
					Toast.LENGTH_LONG).show();
			super.handleMessage(msg);
		}

	};

/*
 * 接收线程
 */
	private class AcceptThread extends Thread
	{
		private BluetoothServerSocket serverSocket;
		private BluetoothSocket socket;
		private InputStream is;
		private OutputStream os;

		public AcceptThread()
		{

			try
			{

//				serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
				Method listenMethod = bluetoothAdapter.getClass().getMethod("listenUsingRfcommOn", new Class[]{int.class});
				serverSocket= ( BluetoothServerSocket) listenMethod.invoke(bluetoothAdapter, new Object[]{ 29});

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

		}

		public void run()
		{

			try
			{
				socket = serverSocket.accept();
				is = socket.getInputStream();
				os = socket.getOutputStream();
				while (true)
				{

					byte[] buffer = new byte[128];
					int count = is.read(buffer);

					Message msg = new Message();
					msg.obj = new String(buffer, 0, count, "utf-8");
					handler.sendMessage(msg);
				}

			}
			catch (Exception e)
			{

			}
		}
	}
	
	public static void writeSDFile(String str,String fileName) {  
        try {  
        	Log.e(TAG, "writeSDFile str = "+str);
            // 打开一个随机访问文件流，按读写方式  
            RandomAccessFile randomFile = new RandomAccessFile(SDPATH + "//" + fileName, "rw");  
            // 文件长度，字节数  
            long fileLength = randomFile.length();  
            // 将写文件指针移到文件尾。  
            randomFile.seek(fileLength);  
            randomFile.writeBytes(str+"\r\n");  
            randomFile.close();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }       
    }  
	
	 public File createSDFile(String fileName) throws IOException { 
	    	File file = new File(SDPATH + "//" + fileName); 
	    	Log.e(TAG, "createSDFile file = "+file);
	    	if (!file.exists()) { 
	    	file.createNewFile(); 
	    	} 
	    	return file; 
	    	} 
}
