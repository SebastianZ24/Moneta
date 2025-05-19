package com.example.moneta.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneta.R;
import com.example.moneta.model.InvestmentHolding;

import java.util.List;
import java.util.Locale;

public class InvestmentHoldingAdapter extends RecyclerView.Adapter<InvestmentHoldingAdapter.InvestmentViewHolder> {

    private List<InvestmentHolding> holdingList;
    private OnInvestmentClickListener clickListener;
    private OnInvestmentLongClickListener longClickListener;

    public InvestmentHoldingAdapter(List<InvestmentHolding> holdingList) {
        this.holdingList = holdingList;
    }

    public void setHoldings(List<InvestmentHolding> holdings) {
        this.holdingList = holdings;
        notifyDataSetChanged();
    }
    public void setOnInvestmentClickListener(OnInvestmentClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnInvestmentLongClickListener(OnInvestmentLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public InvestmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_investment_holding, parent, false);
        return new InvestmentViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull InvestmentViewHolder holder, int position) {
        if (holdingList == null) return;
        InvestmentHolding holding = holdingList.get(position);
        holder.bind(holding, clickListener, longClickListener);
    }

    @Override
    public int getItemCount() {
        return holdingList != null ? holdingList.size() : 0;
    }

    public static class InvestmentViewHolder extends RecyclerView.ViewHolder {
        TextView symbolTextView;
        TextView currentValueTextView;
        TextView quantityPriceTextView;
        TextView profitLossTextView;

        public InvestmentViewHolder(@NonNull View itemView) {
            super(itemView);
            symbolTextView = itemView.findViewById(R.id.investment_symbol_textview);
            currentValueTextView = itemView.findViewById(R.id.investment_current_value_textview);
            quantityPriceTextView = itemView.findViewById(R.id.investment_quantity_price_textview);
            profitLossTextView = itemView.findViewById(R.id.investment_profit_loss_textview);
        }

        public void bind(final InvestmentHolding holding, final OnInvestmentClickListener clickListener, final OnInvestmentLongClickListener longClickListener) {
            if (holding == null) return;

            symbolTextView.setText(holding.getTickerSymbol());
            String qtyPriceStr = String.format(Locale.getDefault(), "%.2f @ %.2f",
                    holding.getQuantity(), holding.getPurchasePrice());
            quantityPriceTextView.setText(qtyPriceStr);

            if (holding.getCurrentPrice() > 0) {
                currentValueTextView.setText(String.format(Locale.getDefault(), "%.2f", holding.getCurrentValue()));
                double profitLoss = holding.getProfitLoss();
                double profitLossPercent = holding.getProfitLossPercent();
                String profitLossStr = String.format(Locale.getDefault(), "%+.2f (%+.2f%%)", profitLoss, profitLossPercent);
                profitLossTextView.setText(profitLossStr);

                int profitLossColor;
                if (profitLoss > 0) {
                    profitLossColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark);
                } else if (profitLoss < 0) {
                    profitLossColor = ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark);
                } else {
                    profitLossColor = ContextCompat.getColor(itemView.getContext(), android.R.color.tab_indicator_text);
                }
                profitLossTextView.setTextColor(profitLossColor);
                currentValueTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.tab_indicator_text));
            } else {
                currentValueTextView.setText("N/A");
                profitLossTextView.setText("N/A");
                int staleColor = ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray);
                currentValueTextView.setTextColor(staleColor);
                profitLossTextView.setTextColor(staleColor);
            }

            if (clickListener != null) {
                itemView.setOnClickListener(v -> {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        clickListener.onInvestmentClick(holding);
                    }
                });
            } else {
                itemView.setOnClickListener(null);
            }

            if (longClickListener != null) {
                itemView.setOnLongClickListener(v -> {
                    if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                        longClickListener.onInvestmentLongClick(holding);
                        return true;
                    }
                    return false;
                });
            } else {
                itemView.setOnLongClickListener(null);
            }
        }
    }

    public interface OnInvestmentClickListener {
        void onInvestmentClick(InvestmentHolding holding);
    }

    public interface OnInvestmentLongClickListener {
        void onInvestmentLongClick(InvestmentHolding holding);
    }
}
