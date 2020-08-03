package com.checkmobi.sdk.ui;

import com.bumptech.glide.Glide;
import com.checkmobi.sdk.model.CountryCode;
import com.checkmobi.sdk.R;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CountryCodeAdapter extends RecyclerView.Adapter<CountryCodeAdapter.ViewHolder> {
    
    public interface CountryCodeListener {
        void onItemSelected(CountryCode countryCode);
    }
    
    private List<CountryCode> mInitialCountryCodes;
    private List<CountryCode> mCountryCodes;
    private CountryCodeListener mCountryCodeListener;
    private CountryCodeFilter mCountryCodeFilter;
    
    public CountryCodeAdapter(List<CountryCode> countryCodes, CountryCodeListener countryCodeListener) {
        mInitialCountryCodes = countryCodes;
        mCountryCodes = countryCodes;
        mCountryCodeListener = countryCodeListener;
        getFilter();
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.country_code_row, parent, false);
        return new ViewHolder(v);
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.setData(holder.itemView.getContext(), mCountryCodes.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCountryCodeListener.onItemSelected(mCountryCodes.get(position));
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return mCountryCodes.size();
    }
    
    public CountryCodeFilter getFilter() {
        if (mCountryCodeFilter == null) {
            mCountryCodeFilter = new CountryCodeFilter();
        }
        
        return mCountryCodeFilter;
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mDescription;
        private ImageView mFlag;
        
        public ViewHolder(View v) {
            super(v);
            mDescription = v.findViewById(R.id.tv_description);
            mFlag = v.findViewById(R.id.iv_flag);
        }
        
        private void setData(Context context, CountryCode countryCode) {
            mDescription.setText(countryCode.getName() + " (+" + countryCode.getPrefix() + ")");
            Glide.with(context)
                    .load(countryCode.getUrl())
                    .into(mFlag);
        }
    }
    
    public class CountryCodeFilter extends Filter {
        
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint!=null && constraint.length()>0) {
                List<CountryCode> tempList = new ArrayList<>();
                
                for (CountryCode countryCode : mInitialCountryCodes) {
                    if (!TextUtils.isEmpty(countryCode.getName())) {
                        String countryNameWithoutParentheses = countryCode.getName().replaceAll("[\\(\\)]", "");
                        String[] countryNames = countryNameWithoutParentheses.split(" ");
                        for (int i=0; i<countryNames.length; i++) {
                            if (countryNames[i].toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                                tempList.add(countryCode);
                                break;
                            }
                        }
                    }
                }
                
                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = mInitialCountryCodes.size();
                filterResults.values = mInitialCountryCodes;
            }
            
            return filterResults;
        }
        
        /**
         * Notify about filtered list to ui
         * @param constraint text
         * @param results filtered result
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mCountryCodes = (ArrayList<CountryCode>) results.values;
            notifyDataSetChanged();
        }
    }
}
