package com.hago.startup.widget.result;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hago.startup.Constant;
import com.hago.startup.MonitorTaskInstance;
import com.hago.startup.R;
import com.hago.startup.db.bean.ResultInfo;
import com.hago.startup.mail.MailInfo;
import com.hago.startup.mail.MailSender;
import com.hago.startup.util.TableUtil;
import com.hago.startup.util.Utils;

import java.util.List;

/**
 * Created by huangzhilong on 18/9/27.
 */

public class ResultView extends RelativeLayout {

    private TextView tvTitle;
    private RecyclerView mRecyclerView;
    private Button btnMail;
    private List<ResultInfo> mResultInfoList;
    private boolean isSendMail = false;
    private ResultAdapter mResultAdapter;

    public ResultView(Context context) {
        this(context, null);
    }

    public ResultView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_result_info, this);
        tvTitle = findViewById(R.id.tv_title);
        mRecyclerView = findViewById(R.id.rcy_list);
        btnMail = findViewById(R.id.btn_mail);
        btnMail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSendMail) {
                    Toast.makeText(getContext(), "已经发送过邮件了!!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Utils.empty(mResultInfoList)) {
                    Toast.makeText(getContext(), "暂无结果!!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                final MailInfo mailInfo = new MailInfo();
                mailInfo.setUserName(Constant.USER); // 你的邮箱地址
                mailInfo.setPassword(Constant.PWD);// 您的邮箱密码
                mailInfo.setToAddress(Constant.TO_ADDRESS); // 发到哪个邮件去
                mailInfo.setSubject("测试结果邮件"); // 邮件主题
                String mailContent = TableUtil.createMailTableText(mResultInfoList);
                mailInfo.setContent(mailContent); // 邮件文本
                final MailSender sms = new MailSender();
                MonitorTaskInstance.getInstance().executeRunnable(new Runnable() {
                    @Override
                    public void run() {
                        boolean result = sms.sendTextMail(mailInfo);
                        if (result) {
                            isSendMail = true;
                            tipShow("邮件发送成功");
                        } else {
                            isSendMail = false;
                            tipShow("邮件发送失败");
                        }
                    }
                });
            }
        });
        LinearLayoutManager mListLinearLayoutManager = new LinearLayoutManager(context);
        mListLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mListLinearLayoutManager);
        mResultAdapter = new ResultAdapter(context);
        mRecyclerView.setAdapter(mResultAdapter);
    }

    private void tipShow(final String tip) {
        MonitorTaskInstance.getInstance().postToMainThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), tip, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //设置结果页面数据
    public void setResultList(List<ResultInfo> resultList, boolean isTarget) {
        if (isTarget) {
            tvTitle.setText("指定包名测试结果");
        } else {
            tvTitle.setText("自动化测试结果");
        }
        mResultInfoList = resultList;
        if (Utils.empty(mResultInfoList)) {
            return;
        }
        mResultAdapter.setData(resultList);
    }
}
