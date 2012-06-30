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
package org.ammlab.android.akbrobotadb;

import java.io.IOException;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

import org.microbridge.server.AbstractServerListener;
import org.microbridge.server.Client;
import org.microbridge.server.Server;


public class AKBrobotADBActivity extends Activity  implements OnClickListener {
    private static final String TAG = "AKBrobotADB";
    private Server server = null;

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
	private static final int MESSAGE_CONNECT = 2;
	private static final int MESSAGE_DISCONNECT = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        
        //�V�[�N�o�[��ύX�����Ƃ��̃n���h��
        //for motor#0
        mSeekBar1.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
        	@Override
        	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        		// �c�}�~���h���b�O�����Ƃ��ɌĂ΂��
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
        		// �c�}�~�ɐG�ꂽ�Ƃ��ɌĂ΂��
        	}
 
        	@Override
        	public void onStopTrackingTouch(SeekBar seekBar) {
        		// ��������j���[�g����
        		mSeekBar1.setProgress(50);
        		//motor#1��~
        		sendCommand((byte)3,(byte)0,(byte)0,(byte)0);
        	}
        });
        //for motor#1
        mSeekBar2.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
        	@Override
        	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        		// �c�}�~���h���b�O�����Ƃ��ɌĂ΂��
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
        		// �c�}�~�ɐG�ꂽ�Ƃ��ɌĂ΂��
        	}
 
        	@Override
        	public void onStopTrackingTouch(SeekBar seekBar) {
        		// ��������j���[�g����
        		mSeekBar2.setProgress(50);
        		//motor#1��~
        		sendCommand((byte)3,(byte)1,(byte)0,(byte)0);
        	}
        });

        //for servo#0
        mSeekBar3.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
        	@Override
        	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        		// �c�}�~���h���b�O�����Ƃ��ɌĂ΂��
        		int value = (180*progress/100);
        		mText3.setText(String.format("%d", value));
        		Log.d(TAG, "Servo0:"+value+","+(byte)value);
        		sendCommand((byte)5,(byte)0,(byte)0,(byte)value);
        	}
 
        	@Override
        	public void onStartTrackingTouch(SeekBar seekBar) {
        		// �c�}�~�ɐG�ꂽ�Ƃ��ɌĂ΂��
        	}
 
        	@Override
        	public void onStopTrackingTouch(SeekBar seekBar) {
        		// ��������Z���^�[��
        		mSeekBar3.setProgress(50);
        		sendCommand((byte)5,(byte)0,(byte)0,(byte)90);
        	}
        });
        //�{�^�����������Ƃ��̃n���h��(�{�^��1)
        mToggleButton1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        		sendCommand((byte)4,(byte)0,(byte)0,(byte)(isChecked ? 0x1 : 0x0));
            }
        });
        //�{�^�����������Ƃ��̃n���h��(�{�^��2)
        mToggleButton2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        		sendCommand((byte)4,(byte)1,(byte)0,(byte)(isChecked ? 0x1 : 0x0));
            }
        });

        // Create TCP server
		try
		{
			server = new Server(4567);
			server.start();
		} catch (IOException e)
		{
			Log.e(TAG, "Unable to start TCP server", e);
			System.exit(-1);
		}

		server.addListener(new AbstractServerListener() {
			@Override
			public void onReceive(org.microbridge.server.Client client, byte[] data)
			{
				Log.d(TAG, "data received:"+data.length);
				if (data.length<4) return;
                Message m = Message.obtain(mHandler, MESSAGE_INPUTVAL);
                synchronized (this) {
                	mRecvBuff[0] = data[0];
                	mRecvBuff[1] = data[1];
                	mRecvBuff[2] = data[2];
                	mRecvBuff[3] = data[3];
                }
                Log.d(TAG, "data received:"+data[0]+","+data[1]+","+data[2]+","+data[3]);
                mHandler.sendMessage(m);
             };
             @Override
             public void onClientConnect(Server server, Client client)
             {
            	 Message m = Message.obtain(mHandler, MESSAGE_CONNECT);
            	 mHandler.sendMessage(m);
             }
             @Override
             public void onClientDisconnect(Server server, Client client)
             {
            	 Message m = Message.obtain(mHandler, MESSAGE_DISCONNECT);
            	 mHandler.sendMessage(m);
             }
		});
		
		enableControls(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(! server.isRunning()){
    		try
    		{
    			server.start();
    		} catch (IOException e)
    		{
    			Log.e(TAG, "Unable to start TCP server", e);
    		}
        }
    }

	@Override
	protected void onPause()
	{
		super.onPause();
		if(server.isRunning())
		{
			server.stop();
		}
	}
    
    public void sendCommand(byte command1, byte command2, byte value1, byte value2) {
		byte[] buffer = new byte[4];
        buffer[0] = command1;
        buffer[1] = command2;
        buffer[2] = value1;
        buffer[3] = value2;
		try
		{
	        server.send(buffer);
		} catch (IOException e)
		{
			Log.e(TAG, "problem sending TCP message", e);
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
    // UI �X���b�h�ŉ�ʏ�̕\����ύX
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
                case MESSAGE_CONNECT:
                    enableControls(true);
                    break;
                case MESSAGE_DISCONNECT:
                    enableControls(false);
                    break;
            }
        }
    };

    // �ڑ���ԕ\��(�^�C�g���o�[)
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