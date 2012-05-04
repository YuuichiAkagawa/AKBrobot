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

/*
 * Work with ksksue's FTDriver
 * https://github.com/ksksue/FTDriver
 * 
 */
package org.ammlab.android.akbrobotuart;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
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
import jp.ksksue.driver.serial.*;

public class AKBrobotUARTActivity extends Activity implements OnClickListener {
    private static final String TAG = "AKBroboUART";
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
    
    
    FTDriver mSerial;
    private boolean mStop=false;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // get FTDI service
        mSerial = new FTDriver((UsbManager)getSystemService(Context.USB_SERVICE));
        // listen for new devices
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);
        
        //初期化失敗
        if(! mSerial.begin(FTDriver.BAUD115200)) {
            unregisterReceiver(mUsbReceiver);
        	this.finish();
        }

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

    }
    
    // BroadcastReceiver when insert/remove the device USB plug into/from a USB port  
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"BroadcastReceiver.onReceive()");
    		String action = intent.getAction();
    		if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
    			mSerial.usbAttached(intent);
				mSerial.begin(FTDriver.BAUD115200);
    		} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
    			mSerial.usbDetached(intent);
    			mSerial.end();
    			mStop=true;
    		}
        }
    };
    
    public void sendCommand(byte command1, byte command2, byte value1, byte value2) {
		//デバイスが停止中の場合は何もしないでリターン
		if( mStop == true){
			return;
		}
		byte[] buffer = new byte[4];
        buffer[0] = command1;
        buffer[1] = command2;
        buffer[2] = value1;
        buffer[3] = value2;
        mSerial.write(buffer,4);       
    }
    public int recvData(byte command1, byte command2) {
		//デバイスが停止中の場合は何もしないでリターン
		if( mStop == true){
			return 0;
		}
		byte[] buffer = new byte[4];
		//flush receive buffer
		while(mSerial.read(buffer) > 0){
			;
		}
		buffer[0] = command1;
        buffer[1] = command2;
        buffer[2] = 0;
        buffer[3] = 0;
        mSerial.write(buffer,4);    
        int rbytes = 0;
        do{
        	try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			rbytes = mSerial.read(buffer);
        }while( rbytes == 0);
        int val = buffer[2] & 0xff;
        val = val * 256 + (buffer[3] & 0xff);
        Log.d(TAG, "data received:"+rbytes+","+buffer[0]+","+buffer[1]+","+buffer[2]+","+buffer[3]+","+val);
        return val;
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
            int val = recvData((byte)2, p);
            Log.d(TAG, "DigitalIn:"+port+","+p+":"+val);
            mInValue.setText(String.valueOf(val));
		}else if( view == mButtonAin){
			long port = mSpinner3.getSelectedItemId();
			byte p = getPort(port, 3);
            int val = recvData((byte)2, p);
            Log.d(TAG, "AnalogIn:"+port+","+p+":"+val);
            mInValue.setText(String.valueOf(val));
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
}