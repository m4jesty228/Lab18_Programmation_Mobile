package com.ensa.viewmodellivedatademoenrichi;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Fragment1_SansViewModel extends Fragment {

    // Variable classique → PERDUE à chaque rotation du fragment
    private int count = 0;
    private TextView tvCount1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sans_viewmodel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvCount1 = view.findViewById(R.id.tvCount1);
        Button btnIncrement1 = view.findViewById(R.id.btnIncrement1);
        Button btnDecrement1 = view.findViewById(R.id.btnDecrement1);
        Button btnReset1 = view.findViewById(R.id.btnReset1);

        // Restauration manuelle avec onSaveInstanceState (ancienne méthode)
        if (savedInstanceState != null) {
            count = savedInstanceState.getInt("count_key", 0);
        }
        updateUI();

        btnIncrement1.setOnClickListener(v -> { count++; updateUI(); });
        btnDecrement1.setOnClickListener(v -> { count--; updateUI(); });
        btnReset1.setOnClickListener(v -> { count = 0; updateUI(); });
    }

    private void updateUI() {
        if (tvCount1 != null) tvCount1.setText(String.valueOf(count));
    }

    // onSaveInstanceState actif → survit à la rotation UNIQUEMENT via Bundle
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("count_key", count);
         //Limitation : seulement types primitifs, pas d'objets complexes
    }
}