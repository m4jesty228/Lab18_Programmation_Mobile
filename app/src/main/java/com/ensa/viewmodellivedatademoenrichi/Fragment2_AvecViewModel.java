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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class Fragment2_AvecViewModel extends Fragment {

    private CounterViewModel viewModel;
    private TextView tvCount2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_avec_viewmodel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvCount2 = view.findViewById(R.id.tvCount2);
        Button btnIncrement2 = view.findViewById(R.id.btnIncrement2);
        Button btnDecrement2 = view.findViewById(R.id.btnDecrement2);
        Button btnReset2 = view.findViewById(R.id.btnReset2);
        Button btnBackgroundThread = view.findViewById(R.id.btnBackgroundThread);

        // ViewModelProvider lie le ViewModel au cycle de vie de l'Activity parente
        // → même instance récupérée après rotation
        viewModel = new ViewModelProvider(requireActivity()).get(CounterViewModel.class);

        // Observer lifecycle-aware → appelé UNIQUEMENT si Fragment en STARTED/RESUMED
        // → zéro crash, zéro memory leak
        viewModel.getCount().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer newCount) {
                tvCount2.setText(String.valueOf(newCount));
            }
        });

        btnIncrement2.setOnClickListener(v -> viewModel.increment());
        btnDecrement2.setOnClickListener(v -> viewModel.decrement());
        btnReset2.setOnClickListener(v -> viewModel.reset());

        // Bonus 1 : postValue depuis un thread background
        btnBackgroundThread.setOnClickListener(v -> viewModel.incrementFromBackground());
    }
}