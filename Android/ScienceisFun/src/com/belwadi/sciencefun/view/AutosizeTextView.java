package com.belwadi.sciencefun.view;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class AutosizeTextView extends TextView implements OnClickListener {
	
	public static int MAX_LINES_ELLIPSIS = 4;
	public static int MAX_LINES_NO_ELLIPSIS = 50;
	
	// Default constructor override
    public AutosizeTextView(Context context) {
        this(context, null);
        this.setOnClickListener(this);
    }

    // Default constructor when inflating from XML file
    public AutosizeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.setOnClickListener(this);
    }

    // Default constructor override
    public AutosizeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setOnClickListener(this);
    }
    
    private void onClickTextView()
	{
		if (checkTextViewEllipse())
			this.setMaxLines(MAX_LINES_NO_ELLIPSIS);
		else
			this.setMaxLines(MAX_LINES_ELLIPSIS);
	}
	
	public boolean checkTextViewEllipse()
	{
		Layout l = this.getLayout();
        if ( l != null){
            int lines = l.getLineCount();
            
            if (lines < MAX_LINES_ELLIPSIS)
            	return false;
            
            if ( lines > 0) {
                if ( l.getEllipsisCount(MAX_LINES_ELLIPSIS-1) > 0)
//                if ( l.getEllipsisCount(lines-1) > 0)
                	return true;
            }
        }
        
        return false;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		onClickTextView();
	}

}
