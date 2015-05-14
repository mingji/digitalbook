package com.belwadi.sciencefun.ui;

import java.util.ArrayList;

import com.belwadi.sciencefun.R;
import com.belwadi.sciencefun.database.DatabaseManager;
import com.belwadi.sciencefun.entity.*;
import com.belwadi.sciencefun.utils.ImageLoader;
import com.belwadi.sciencefun.view.HyIconView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Spinner;
import android.widget.TextView;

public class LessonFragment extends Fragment
{
	private final int CONCEPT_ACTIVITY = 0;
	private ArrayList<Concept> mArrayConcept = null;
	private ConceptAdapter mAdapterConcept;
	private ListView mListviewConcept;
	
	private Spinner mSpinChapter;
	private ArrayList<String> mArrayStrChapter;
	private ArrayAdapter<String> mAdapterChapter;
	private ArrayList<Chapter> mArrayChapter;
	
	private String mSyllabusId, mGradeId;
	
	private DatabaseManager mDbMgr;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_lessons, null);
		
		mDbMgr = new DatabaseManager(getActivity());

		initView(view);
		
		Syllabus currentSyllabus = GlobalValue.getInstance().getCurrentSyllabus();
		mSyllabusId = currentSyllabus.id;
		
		Grade currentGrade = GlobalValue.getInstance().getCurrentGrade();
		mGradeId = currentGrade.id;
		
		updateChapter();

		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
//		Syllabus currentSyllabus = GlobalValue.getInstance().getCurrentSyllabus();
//		mSyllabusId = currentSyllabus.id;
//		
//		Grade currentGrade = GlobalValue.getInstance().getCurrentGrade();
//		mGradeId = currentGrade.id;
//		
//		updateChapter();
	}

	private void initView(View view)
	{
		mSpinChapter = (Spinner) view.findViewById(R.id.spinChapter);
		mSpinChapter.setPrompt("Select Chapter");
		
		mArrayStrChapter = new ArrayList<String>();
		mArrayChapter = new ArrayList<Chapter>();
		mAdapterChapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mArrayStrChapter);
		mAdapterChapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
		mSpinChapter.setAdapter(mAdapterChapter);
		mSpinChapter.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long id) {
            	Chapter chapter = mArrayChapter.get(pos);
            	updateConcept(chapter.id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

		mArrayConcept = new ArrayList<Concept>();
		mAdapterConcept = new ConceptAdapter(getActivity(), R.layout.item_concept, mArrayConcept);

		mListviewConcept = (ListView) view.findViewById(R.id.lstConcept);		
		mListviewConcept.setAdapter(mAdapterConcept);
		mListviewConcept.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int position, long id) {
				Concept item = mArrayConcept.get(position);
				startConceptActivity(item.id);
			}
		});		
	}
	
	public void updateChapter()
	{
		Grade currentGrade = GlobalValue.getInstance().getCurrentGrade();
		mGradeId = currentGrade.id;
		
		mArrayChapter = mDbMgr.getChapterByGrade(mGradeId);
		mArrayStrChapter.clear();
		for (Chapter chapter : mArrayChapter)
		{
			mArrayStrChapter.add(chapter.title);
		}
		mAdapterChapter.notifyDataSetChanged();
		
		if (mSpinChapter.getSelectedItemPosition() >= 0)
			updateConcept(null);
	}
	
	private void updateConcept(String chapterId)
	{
		String currentChapterId = chapterId;
		if (currentChapterId == null)
			currentChapterId = mArrayChapter.get(mSpinChapter.getSelectedItemPosition()).id;
		
		mArrayConcept.clear();
		ArrayList<Concept> tmp = mDbMgr.getConceptByParam(mSyllabusId, mGradeId, currentChapterId);
		for (Concept concept : tmp) {
			mArrayConcept.add(concept);
		}
		
		mAdapterConcept.notifyDataSetChanged();
	}

	private void startConceptActivity(String conceptId) {
		// Run next activity
		Intent intent = new Intent();
		intent.setClass(getActivity(), ConceptActivity.class);
		intent.putExtra("concept_id", conceptId);
		startActivityForResult(intent, CONCEPT_ACTIVITY);
		getActivity().overridePendingTransition(R.anim.in_right_left, R.anim.out_right_left);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(resultCode != Activity.RESULT_OK)
			return;

		if (requestCode == CONCEPT_ACTIVITY) {
			String goPage = data.getStringExtra("go_page");

			if (goPage != null) {
				HomeActivity parentActivity = (HomeActivity)getActivity();
				parentActivity.porgressResult(goPage);				
			}
		}
	}
	
	public class ConceptAdapter extends ArrayAdapter<Concept> {
		Activity activity;
		int layoutResourceId;
		ArrayList<Concept> item = new ArrayList<Concept>();

		// constuctor
		public ConceptAdapter(Activity activity, int layoutId, ArrayList<Concept> items) {
			super(activity, layoutId, items);
			item = items;
			this.activity = activity;
			this.layoutResourceId = layoutId;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ConceptHolder holder = null;	

			final Concept conceptItem = getItem(position);

			// inflate the view
//			if (convertView == null) {
				LayoutInflater inflater = ((Activity) activity).getLayoutInflater();
				convertView = inflater.inflate(R.layout.item_concept, null);
				holder = new ConceptHolder();
				holder.txtName = (TextView) convertView.findViewById(R.id.txtConceptName);
				holder.imgPic = (HyIconView) convertView.findViewById(R.id.imgConcept);
				holder.imgPic.failedBp = BitmapFactory.decodeResource(getResources(), R.drawable.icon_image_not);
				holder.imgPic.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {						
						startConceptActivity(conceptItem.id);
					}
				});
				holder.txtCredit = (TextView) convertView.findViewById(R.id.txtCredit);

				convertView.setTag(holder);
//			} 
//			else
//				holder = (ConceptHolder) convertView.getTag();

			holder.txtName.setText(conceptItem.title);
			
			holder.txtCredit.setClickable(true);
			holder.txtCredit.setMovementMethod(LinkMovementMethod.getInstance());
			String creditHtml = "<a href='" + conceptItem.image_source + "'>" +  conceptItem.image_credit + "</a>";
			holder.txtCredit.setText(Html.fromHtml(creditHtml));
			
			try {
				if (holder.imgPic != null)
					ImageLoader.getInstance().displayImage(conceptItem.image, holder.imgPic, 0, 0);
			} catch (Exception e){
				Log.d("", e.toString());
			}
			return convertView;
		}

		public class ConceptHolder {
			TextView txtName;
			HyIconView imgPic;
			TextView txtCredit;
		}
	}
}
