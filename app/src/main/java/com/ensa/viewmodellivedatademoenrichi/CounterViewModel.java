package com.ensa.viewmodellivedatademoenrichi;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CounterViewModel extends ViewModel {

    // MutableLiveData : modifiable uniquement depuis le ViewModel
    private final MutableLiveData<Integer> countLiveData = new MutableLiveData<>();

    public CounterViewModel() {
        countLiveData.setValue(0); // Valeur initiale, appelée une seule fois
    }

    public void increment() {
        Integer current = countLiveData.getValue();
        if (current != null) countLiveData.setValue(current + 1);
    }

    public void decrement() {
        Integer current = countLiveData.getValue();
        if (current != null) countLiveData.setValue(current - 1);
    }

    public void reset() {
        countLiveData.setValue(0);
    }

    // Bonus 1 : postValue — safe depuis n'importe quel thread background
    public void incrementFromBackground() {
        new Thread(() -> {
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
            Integer current = countLiveData.getValue();
            if (current != null) countLiveData.postValue(current + 1);
        }).start();
    }

    // Getter exposé en lecture seule à l'Activity/Fragment
    public LiveData<Integer> getCount() {
        return countLiveData;
    }
}