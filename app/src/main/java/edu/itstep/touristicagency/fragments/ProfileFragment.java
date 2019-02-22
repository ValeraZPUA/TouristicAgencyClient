package edu.itstep.touristicagency.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.google.gson.Gson;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import edu.itstep.touristicagency.MainActivity;
import edu.itstep.touristicagency.R;
import edu.itstep.userdata.UserInfo;

public class ProfileFragment extends Fragment implements View.OnClickListener
{
    private EditText etEmail, etPassword, etConfPass, etName, etLastname, etCellnumber;
    private EditText[] et;
    private Button[] buttons;
    private Button btnOk, btnCancel, btnEdit;
    private PrintWriter printWriter;
    private Socket socket;
    private Bundle bundle;
    private Gson gson;
    private UserInfo  userInfo;
    private String[] newUserInfo;
    private String[] updateUserInfoRequestToServer;
    private String gsonStringupdateUserInfoRequestToServer;
    private String index;

    private String gsonStingFromServerUserInfo;

    public ProfileFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        MainActivity ma = (MainActivity) getActivity();
        socket = ma.getSocket();

        et = new EditText[6];
        et[0]=etEmail;
        et[1]=etPassword;
        et[2]=etConfPass;
        et[3]=etName;
        et[4]=etLastname;
        et[5]=etCellnumber;

        et[0] = view.findViewById(R.id.etEmailPF);
        et[1] = view.findViewById(R.id.etPasswordPF);
        et[2] = view.findViewById(R.id.etConfPasswordPF);
        et[3] = view.findViewById(R.id.etNamePF);
        et[4] = view.findViewById(R.id.etLastNamePF);
        et[5] = view.findViewById(R.id.etCellnumberPF);

        buttons = new Button[3];
        buttons[0] = btnOk;
        buttons[1] = btnCancel;
        buttons[2] = btnEdit;

        buttons[0] = view.findViewById(R.id.btnOk);
        buttons[1] = view.findViewById(R.id.btnCancel);
        buttons[2] = view.findViewById(R.id.btnEdit);

        gson = new Gson();

        for(int i=0;i<buttons.length;i++)
        {
            buttons[i].setOnClickListener(this);
        }

        actions();

        return view;
    }

    private void actions()
    {
        getBundle();
        fillUserInfo();
    }

    private void getBundle()
    {
        bundle = getArguments();
        gsonStingFromServerUserInfo = bundle.getString(MainActivity.KEY_USER_INFO);
        userInfo = gson.fromJson(gsonStingFromServerUserInfo, UserInfo.class);
        Log.d("tag", "PROFILE getBundle: " + gsonStingFromServerUserInfo);
    }

    private void fillUserInfo()
    {
        et[0].setText(userInfo.geteMail());
        et[3].setText(userInfo.getName());
        et[4].setText(userInfo.getSurname());
        et[5].setText(userInfo.getCellnumber());
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btnOk:
                checkСhanges();
                getFragmentManager().popBackStack();
                break;
            case R.id.btnCancel:
                getFragmentManager().popBackStack();
                break;
            case R.id.btnEdit:
                setEditableTrue();
                break;
        }
    }

    private void checkСhanges()
    {
        index = "6";
        String newEmail = et[0].getText().toString();
        String newPass =  et[1].getText().toString();
        String newName = et[3].getText().toString();
        String newLastName = et[4].getText().toString();
        String newCellNumber = et[5].getText().toString();
        newUserInfo = new String[]{index, userInfo.getId(), newEmail, newPass, newName, newLastName, newCellNumber};

        String[] array = new String[6];
        array[0]=userInfo.getId();

        if(userInfo.geteMail().equals(newEmail))
        {
            newUserInfo[2]="0";
            array[1]=userInfo.geteMail();
        }
        else
        {
            array[1]=newEmail;
        }
        if(newPass.equals(getString(R.string.password)))
        {
            newUserInfo[3]="0";
            array[2]=userInfo.getPassword();
        }
        else
        {
            array[2]=newPass;
        }
        if(userInfo.getName().equals(newName))
        {
            newUserInfo[4]="0";
            array[3]=userInfo.getName();
        }
        else
        {
            array[3]=newName;
        }
        if(userInfo.getSurname().equals(newLastName))
        {
            newUserInfo[5]="0";
            array[4]=userInfo.getSurname();
        }
        else
        {
            array[4]=newLastName;
        }
        if(userInfo.getCellnumber().equals(newCellNumber))
        {
            newUserInfo[6]="0";
            array[5]=userInfo.getCellnumber();
        }
        else
        {
            array[5]=newCellNumber;
        }
        Log.d("tag", "PROFILE new user info: " + newUserInfo[1] + " " + newUserInfo[2] + " " + newUserInfo[3] + " " +
                                newUserInfo[4] + " " + newUserInfo[5] + " " + newUserInfo[6]);

        for(int i=2;i<newUserInfo.length;i++)
        {
            Log.d("tag", "PROFILE newUserInfo " + i + " " + newUserInfo[i]);
            if(!newUserInfo[i].equals("0"))
            {
                UpdateUserInfoAsyncTask updateUserInfoAsyncTask = new UpdateUserInfoAsyncTask();
                updateUserInfoAsyncTask.execute(newUserInfo);
                break;
            }
        }
    }

    private void setEditableTrue()
    {
        for(int i=0;i<et.length;i++)
        {
            et[i].setEnabled(true);
        }
    }

    private class UpdateUserInfoAsyncTask extends AsyncTask<String[], Void, Void>
    {
        @Override
        protected Void doInBackground(String[]... params)
        {

            try
            {
                updateUserInfoRequestToServer = params[0];
                printWriter = new PrintWriter(new BufferedOutputStream(socket.getOutputStream()));

            } catch (IOException e)
            {
                e.printStackTrace();
            }

            send(updateUserInfoRequestToServer);
            return null;
        }

        private void send(String[] updateUserInfoRequestToServer)
        {
            gsonStringupdateUserInfoRequestToServer = gson.toJson(updateUserInfoRequestToServer);
            printWriter.println(gsonStringupdateUserInfoRequestToServer);
            printWriter.flush();
        }
    }
}
