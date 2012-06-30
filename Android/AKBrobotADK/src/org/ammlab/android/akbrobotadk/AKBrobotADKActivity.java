/*
  Copyright 2012 JAG-AKIBA

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package org.ammlab.android.akbrobotadk;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class AKBrobotADKActivity extends Activity  implements OnClickListener, Runnable {
    private static final String TAG = "AKBrobotADK";
    private static final String ACTION_USB_PERMISSION = "org.ammlab.android.app.akbrobotadk.action.USB_PERMISSION";
    private PendingIntent mPermissionIntent;
    private boolean mPermissionRequestPending;

    private UsbManager mUsbManager;
    private UsbAccessory mAccessory;

    ParcelFileDescriptor mFileDescriptor = null;

    FileInputStream mInputStream = null;
    FileOutputStream mOutputStream = null;
    private ToggleButton mToggleButton1;
    private ToggleButton mToggleButton2;
    private SeekBar mSeekBar1;
    private SeekBar mSeekBar2;
    private SeekBar mSeekBar3;
    private TextView mText1;
    private TextView mText2;
    private TextView mText3;
    private TextView mInValue;
    private EditText mEditOutput;
    private Spinner  mSpinner1;
    private Spinner  mSpinner2;
    private Spinner  mSpinner3;
    private Spinner  mSpinner4;
    private	Button	 mButtonConfig;
    private	Button	 mButtonDin;
    private	Button	 mButtonAin;
    private	Button	 mButtonDout;
    private	Button	 mButtonAout;
    
    private byte[] mRecvBuff = new byte[4];
    
    private static final int MESSAGE_INPUTVAL = 1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // UsbManager のインスタンスを取得
        mUsbManager = UsbManager.getInstance(this);

        // オレオレパーミッション用 Broadcast Intent
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        // オレオレパーミッション Intent とアクセサリが取り外されたときの Intent を登録
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(mUsbReceiver, filter);

        //re-creation?
        if (getLastNonConfigurationInstance() != null) {
        	mAccessory = (UsbAccessory) getLastNonConfigurationInstance();
        	openAccessory(mAccessory);
        }
        
        setContentView(R.layout.main);

        mToggleButton1 = (ToggleButton) findViewById(R.id.toggleButton1);
        mToggleButton2 = (ToggleButton) findViewById(R.id.toggleButton2);
        mSeekBar1 = (SeekBar) findViewById(R.id.seekBar1);
        mSeekBar2 = (SeekBar) findViewById(R.id.seekBar2);
        mSeekBar3 = (SeekBar) findViewById(R.id.seekBar3);
        mSeekBar1.setProgress(50);
        mSeekBar2.setProgress(50);
        mSeekBar3.setProgress(50);

        mText1    = (TextView) findViewById(R.id.textView1);
        mText2    = (TextView) findViewById(R.id.textView2);
        mText3    = (TextView) findViewById(R.id.textView3);
        mInValue  = (TextView) findViewById(R.id.labelinvalue);

        mEditOutput = (EditText) findViewById(R.id.editOutput);

        mSpinner1 = (Spinner) findViewById(R.id.spinner1);
        mSpinner2 = (Spinner) findViewById(R.id.spinner2);
        mSpinner3 = (Spinner) findViewById(R.id.spinner3);
        mSpinner4 = (Spinner) findViewById(R.id.spinner4);

        mButtonConfig = (Button)findViewById(R.id.buttonconfig); 
        mButtonDin    = (Button)findViewById(R.id.buttondin); 
        mButtonAin    = (Button)findViewById(R.id.buttonain); 
        mButtonDout   = (Button)findViewById(R.id.buttondout); 
        mButtonAout   = (Button)findViewById(R.id.buttonaout); 

        mButtonConfig.setOnClickListener(this);
        mButtonDin.setOnClickListener(this);
        mButtonAin.setOnClickListener(this);
        mButtonDout.setOnClickListener(this);
        mButtonAout.setOnClickListener(this);
        
        //シークバーを変更したときのハンドラ
        //for motor#0
        mSeekBar1.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
        	@Override
        	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        		// ツマミをドラッグしたときに呼ばれる
        		int  value = 0;
        		byte direction = 0;
        		if( progress > 45 && progress < 55){
        			value = 0;
        		}else if(progress > 50){
        			value = ((progress - 50) * 255 / 50);
        			direction = 0;
        		}else{ //progress < 50
        			value = ((50 - progress) * 255 / 50);
        			direction = 1;
        		}
        		mText1.setText(String.format("%d", value));
        		sendCommand((byte)3,(byte)0,direction,(byte)value);
        	}
 
        	@Override
        	public void onStartTrackingTouch(SeekBar seekBar) {
        		// ツマミに触れたときに呼ばれる
        	}
 
        	@Override
        	public void onStopTrackingTouch(SeekBar seekBar) {
        		// 離したらニュートラル
        		mSeekBar1.setProgress(50);
        		//motor#1停止
        		sendCommand((byte)3,(byte)0,(byte)0,(byte)0);
        	}
        });
        //for motor#1
        mSeekBar2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
        	@Override
        	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        		// ツマミをドラッグしたときに呼ばれる
        		int  value = 0;
        		byte direction = 0;
        		if( progress > 45 && progress < 55){
        			value = 0;
        		}else if(progress > 50){
        			value = ((progress - 50) * 255 / 50);
        			direction = 0;
        		}else{ //progress < 50
        			value = ((50 - progress) * 255 / 50);
        			direction = 1;
        		}
        		mText2.setText(String.format("%d", value));
        		sendCommand((byte)3,(byte)1,direction,(byte)value);
        	}
 
        	@Override
        	public void onStartTrackingTouch(SeekBar seekBar) {
        		// ツマミに触れたときに呼ばれる
        	}
 
        	@Override
        	public void onStopTrackingTouch(SeekBar seekBar) {
        		// 離したらニュートラル
        		mSeekBar2.setProgress(50);
        		//motor#1停止
        		sendCommand((byte)3,(byte)1,(byte)0,(byte)0);
        	}
        });

        //for servo#0
        mSeekBar3.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
        	@Override
        	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        		// ツマミをドラッグしたときに呼ばれる
        		int value = (180*progress/100);
        		mText3.setText(String.format("%d", value));
        		Log.d(TAG, "Servo0:"+value+","+(byte)value);
        		sendCommand((byte)5,(byte)0,(byte)0,(byte)value);
        	}
 
        	@Override
        	public void onStartTrackingTouch(SeekBar seekBar) {
        		// ツマミに触れたときに呼ばれる
        	}
 
        	@Override
        	public void onStopTrackingTouch(SeekBar seekBar) {
        		// 離したらセンターに
        		mSeekBar3.setProgress(50);
        		sendCommand((byte)5,(byte)0,(byte)0,(byte)90);
        	}
        });
        //ボタンを押したときのハンドラ(ボタン1)
        mToggleButton1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        		sendCommand((byte)4,(byte)0,(byte)0,(byte)(isChecked ? 0x1 : 0x0));
            }
        });
        //ボタンを押したときのハンドラ(ボタン2)
        mToggleButton2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        		sendCommand((byte)4,(byte)1,(byte)0,(byte)(isChecked ? 0x1 : 0x0));
            }
        });

        enableControls(false);
    }
    
    @Override
    public void onResume() {
        super.onResume();

        if (mInputStream != null && mOutputStream != null) {
            return;
        }

        // USB Accessory の一覧を取得
        UsbAccessory[] accessories = mUsbManager.getAccessoryList();
        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory != null) {
            // Accessory にアクセスする権限があるかチェック
            if (mUsbManager.hasPermission(accessory)) {
                // 接続を開く
                openAccessory(accessory);
            } else {
                synchronized (mUsbReceiver) {
                    if (!mPermissionRequestPending) {
                        // パーミッションを依頼
                        mUsbManager.requestPermission(accessory, mPermissionIntent);
                        mPermissionRequestPending = true;
                    }
                }
            }
        } else {
            Log.d(TAG, "mAccessory is null");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        closeAccessory();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mUsbReceiver);
        super.onDestroy();
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    // Intent からアクセサリを取得
                    UsbAccessory accessory = UsbManager.getAccessory(intent);

                    // パーミッションがあるかチェック
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        // 接続を開く
                        openAccessory(accessory);
                    } else {
                        Log.d(TAG, "permission denied for accessory " + accessory);
                    }
                    mPermissionRequestPending = false;
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                // Intent からアクセサリを取得
                UsbAccessory accessory = UsbManager.getAccessory(intent);
                if (accessory != null && accessory.equals(mAccessory)) {
                    // 接続を閉じる
                    closeAccessory();
                }
            }
        }
    };

    private void openAccessory(UsbAccessory accessory) {
        // アクセサリにアクセスするためのファイルディスクリプタを取得
        mFileDescriptor = mUsbManager.openAccessory(accessory);

        if (mFileDescriptor != null) {
            mAccessory = accessory;
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();

            // 入出力用のストリームを確保
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);

            // この中でアクセサリとやりとりする
            Thread thread = new Thread(null, this, "DemoKit");
            thread.start();
            enableControls(true);
            Log.d(TAG, "accessory opened");
        } else {
            Log.d(TAG, "accessory open fail");
        }
    }

    private void closeAccessory() {
        enableControls(false);
        try {
            if (mFileDescriptor != null) {
                mInputStream.close();
                mOutputStream.close();
                mFileDescriptor.close();
            }
        } catch (IOException e) {
        } finally {
            mFileDescriptor = null;
            mAccessory = null;
            mInputStream = null;
            mOutputStream = null;
            Log.d(TAG, "accessory closed");
        }
    }

    @Override
	public void onClick(View view) {
		if( view == mButtonConfig ){
			long port = mSpinner1.getSelectedItemId();
			long mode = mSpinner2.getSelectedItemId();
			byte p = getPort(port, 256);
            Log.d(TAG, "Primitive:"+port+","+p+","+mode);
            sendCommand((byte)0, p, (byte)0, (byte)mode);
		}else if( view == mButtonDout){
			long port = mSpinner4.getSelectedItemId();
			String value = mEditOutput.getText().toString();
			byte p = getPort(port, 0);
			if( value != null && value.length() > 0){
				byte v = (byte)(int)Integer.valueOf(value);
	            Log.d(TAG, "DigitalOut:"+port+","+p+","+v);
				sendCommand((byte)1, p, (byte)0, (byte)v);
			}
		}else if( view == mButtonAout){
			long port = mSpinner4.getSelectedItemId();
			String value = mEditOutput.getText().toString();
			byte p = getPort(port, 1);
			if( value != null && value.length() > 0){
				byte v = (byte)(int)Integer.valueOf(value);
	            Log.d(TAG, "AnalogOut:"+port+","+p+","+v);
				sendCommand((byte)1, p, (byte)0, (byte)v);
			}
		}else if( view == mButtonDin){
			long port = mSpinner3.getSelectedItemId();
			byte p = getPort(port, 2);
            sendCommand((byte)2, p, (byte)0, (byte)0);
            Log.d(TAG, "DigitalIn:"+port+","+p);
		}else if( view == mButtonAin){
			long port = mSpinner3.getSelectedItemId();
			byte p = getPort(port, 3);
            sendCommand((byte)2, p, (byte)0, (byte)0);
            Log.d(TAG, "AnalogIn:"+port+","+p);
		}else{
			Log.d(TAG, "Ignore click:"+view.toString());
		}
	}
	
	private byte getPort(long port, long type){//type(0:DigitalOut, 1:AnalogOut, 2:DigitalIn, 3:AnalogIn, 256:config)
		byte ConfigPort=-1;
		long limitDigtal = 6;
		if(type == 0 || type == 2 || type == 256){ //Digital Port
			if(port > limitDigtal){ //Analog Port
				ConfigPort = (byte) (port - limitDigtal -1 + 0x40);
			}else{
				ConfigPort = (byte) port;
			}
		}else if( type == 1 ){//Analog Out Port
			ConfigPort = (byte)(port + 0x80);
		}else{ //AnalogIn Port
			if( port > limitDigtal){
				ConfigPort = (byte)(port - limitDigtal -1 + 0x80);
			}
		} 
		return ConfigPort;
	}
    // ここでアクセサリと通信する
    @Override
    public void run() {
        int ret = 0;
        byte[] buffer = new byte[16384];
        int i;

        Log.d(TAG, "Thread started.");
        // アクセサリ -> アプリ
        while (mInputStream != null) {
//        while (ret >= 0) {
            try {
                ret = mInputStream.read(buffer);
            } catch (IOException e) {
                break;
            }

            if( ret > 0 ){
                Log.d(TAG, ret + " bytes message received.");
            }
            i = 0;
            while (i < ret) {
                int len = ret - i;

                switch (buffer[i]) {
                    case 0x2:
                        if (len >= 4) {
                            Message m = Message.obtain(mHandler, MESSAGE_INPUTVAL);
                            synchronized (this) {
                            	mRecvBuff[0] = buffer[0];
                            	mRecvBuff[1] = buffer[1];
                            	mRecvBuff[2] = buffer[2];
                            	mRecvBuff[3] = buffer[3];
                            }
                            Log.d(TAG, "data received:"+buffer[0]+","+buffer[1]+","+buffer[2]+","+buffer[3]);
                            mHandler.sendMessage(m);
                            i += 4;
                        }
                        break;

                    default:
                        Log.d(TAG, "unknown msg: " + buffer[i]);
                        i = len;
                        break;
                }
            }

        }
        Log.d(TAG, "Thread end.");
    }
    // アプリ -> アクセサリ
    public void sendCommand(byte command1, byte command2, byte value1, byte value2) {
        byte[] buffer = new byte[4];
        buffer[0] = command1;
        buffer[1] = command2;
        buffer[2] = value1;
        buffer[3] = value2;

        if (mOutputStream != null) {
            try {
                mOutputStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "write failed", e);
            }
        }
    }
    // UI スレッドで画面上の表示を変更
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_INPUTVAL:
            		int val=0;
                	synchronized (this) {
                        val = mRecvBuff[2] & 0xff;
                        val = val * 256 + (mRecvBuff[3] & 0xff);
                	}
               		mInValue.setText(String.valueOf(val));
                   break;
            }
        }
    };
    
    // 接続状態表示(タイトルバー)
    private void enableControls(boolean enable) {
    	Resources res = getResources();
    	String titleString = res.getString(R.string.app_name);
 
        if (enable) {
            setTitle(titleString + " - connected");
        } else {
            setTitle(titleString + " - not connected");
        }
    }
}