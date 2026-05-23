# Lab 18 – ViewModel et LiveData en Android

**Cours :** Programmation Mobile – Android avec Java
**Date :** Mai 2026

---

## 1. Introduction

Dans ce laboratoire, j'ai exploré et mis en pratique les concepts fondamentaux de l'architecture Android moderne : **ViewModel** et **LiveData**. L'objectif central était de comprendre pourquoi les variables classiques sont perdues à chaque rotation d'écran, de voir concrètement les limites de l'ancienne méthode `onSaveInstanceState()`, et de maîtriser l'approche Jetpack recommandée par Google.

L'application développée regroupe les **deux parties du lab dans une seule app** avec deux onglets distincts, permettant une démonstration visuelle et comparative immédiate :

- **Onglet 1 ⚠️ Sans ViewModel** : compteur classique avec variable d'instance, illustrant le problème de la perte de données à la rotation
- **Onglet 2 ✅ Avec ViewModel** : compteur propulsé par `CounterViewModel` + `LiveData`, démontrant la persistance automatique et la mise à jour réactive de l'UI

---

## 2. Concepts clés abordés

### Pourquoi une variable classique est perdue à la rotation ?

Quand l'écran tourne, Android **détruit** l'Activity en cours (`onDestroy`) et en **recrée une nouvelle** (`onCreate`). Toutes les variables d'instance sont alors réinitialisées. C'est le comportement par défaut du système pour adapter l'interface à la nouvelle orientation.

### `onSaveInstanceState()` — l'ancienne solution et ses limites

`onSaveInstanceState()` permet de sauvegarder manuellement des données dans un `Bundle` avant la destruction. Mais cette approche est **limitée** :
- Seulement les types primitifs (`int`, `String`, `boolean`...)
- Pas d'objets complexes, pas de threads, pas de LiveData
- Code verbeux et source d'erreurs

### ViewModel — la solution moderne

`ViewModel` est un composant Jetpack lié au `ViewModelStore` de l'Activity. Il **survit à la rotation** et à la destruction/re-création temporaire de l'Activity. Il n'est détruit que lorsque l'Activity est définitivement terminée (ex: l'utilisateur appuie sur Retour).

### LiveData — l'observable lifecycle-aware

`LiveData` est un conteneur de données observable qui respecte le cycle de vie. Il ne notifie les observers **que si l'Activity/Fragment est en état `STARTED` ou `RESUMED`**, ce qui garantit :
- Zéro crash lié à une UI détruite
- Zéro memory leak
- Mise à jour automatique de l'UI

### MutableLiveData vs LiveData

| | `MutableLiveData` | `LiveData` |
|---|---|---|
| Modifiable | ✅ Oui | ❌ Non |
| Utilisé dans | ViewModel (privé) | Exposé à l'Activity/Fragment (lecture seule) |
| Méthodes | `setValue()`, `postValue()` | `observe()` |

### `setValue()` vs `postValue()`

- **`setValue()`** : à appeler uniquement depuis le **thread principal**
- **`postValue()`** : à appeler depuis n'importe quel **thread background** — il dispatche la valeur sur le thread principal automatiquement

---

## 3. Environnement de travail

- **IDE :** Android Studio
- **Langage :** Java
- **SDK minimum :** API 24 (Android 7.0)
- **SDK cible :** API 36
- **Jetpack Lifecycle :** 2.10.0

---

## 4. Structure du projet

```
ViewModelLiveDataDemoEnrichi/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/ensa/viewmodellivedatademoenrichi/
│   │       │   ├── MainActivity.java               ← TabLayout + ViewPager2
│   │       │   ├── ViewPagerAdapter.java            ← Adaptateur des onglets
│   │       │   ├── Fragment1_SansViewModel.java     ← Partie 1 (variable classique)
│   │       │   ├── Fragment2_AvecViewModel.java     ← Partie 2 (ViewModel + LiveData)
│   │       │   └── CounterViewModel.java            ← ViewModel partagé
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   ├── activity_main.xml            ← TabLayout + ViewPager2
│   │       │   │   ├── fragment_sans_viewmodel.xml  ← Layout Partie 1
│   │       │   │   └── fragment_avec_viewmodel.xml  ← Layout Partie 2
│   │       │   └── values/
│   │       │       ├── strings.xml
│   │       │       └── themes.xml
│   │       └── AndroidManifest.xml
```

---

## 5. Dépendances ajoutées (build.gradle)

```kotlin
val lifecycle_version = "2.10.0"
implementation("androidx.lifecycle:lifecycle-viewmodel:$lifecycle_version")
implementation("androidx.lifecycle:lifecycle-livedata:$lifecycle_version")
implementation("androidx.viewpager2:viewpager2:1.1.0")
```

---

## 6. Architecture de l'application

L'application suit le pattern **MVVM (Model-View-ViewModel)** recommandé par Google :

```
MainActivity
├── TabLayout + ViewPager2
├── Fragment1_SansViewModel   →  variable int count (classique)
└── Fragment2_AvecViewModel   →  observe CounterViewModel
                                        ↕ LiveData<Integer>
                                  CounterViewModel
                                  (MutableLiveData, setValue, postValue)
```

---

## 7. Partie 1 — Sans ViewModel (le problème classique)

### Fonctionnement

Le compteur est stocké dans une simple variable d'instance `private int count = 0` dans le Fragment. Trois boutons permettent d'incrémenter, décrémenter et réinitialiser la valeur. La méthode `updateUI()` met à jour le `TextView` manuellement à chaque action.

`onSaveInstanceState()` est utilisé pour sauvegarder et restaurer la valeur via un `Bundle` (clé `"count_key"`).

### Code clé

```java
// Variable classique → PERDUE à la rotation sans onSaveInstanceState
private int count = 0;

// Restauration manuelle
if (savedInstanceState != null) {
    count = savedInstanceState.getInt("count_key", 0);
}

// Sauvegarde manuelle avant destruction
@Override
public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt("count_key", count);
    // Limitation : seulement int, pas d'objet complexe, pas de threads
}
```

---

## 8. Partie 2 — Avec ViewModel + LiveData (la solution moderne)

### Fonctionnement

Le compteur est géré par `CounterViewModel` qui expose un `MutableLiveData<Integer>`. Le Fragment observe ce `LiveData` via `getViewLifecycleOwner()` — l'observer est automatiquement supprimé quand le Fragment est détruit, sans aucune fuite mémoire.

Après une rotation, `ViewModelProvider` retourne **la même instance** du ViewModel déjà existante — les données sont intactes.

### Code clé — CounterViewModel.java

```java
public class CounterViewModel extends ViewModel {

    private final MutableLiveData<Integer> countLiveData = new MutableLiveData<>();

    public CounterViewModel() {
        countLiveData.setValue(0); // Initialisé une seule fois
    }

    public void increment() {
        Integer current = countLiveData.getValue();
        if (current != null) countLiveData.setValue(current + 1);
    }

    // postValue : safe depuis un thread background
    public void incrementFromBackground() {
        new Thread(() -> {
            countLiveData.postValue(countLiveData.getValue() + 1);
        }).start();
    }

    // Exposé en lecture seule → bonne pratique
    public LiveData<Integer> getCount() {
        return countLiveData;
    }
}
```

### Code clé — Fragment2_AvecViewModel.java

```java
// Récupération (ou création) du ViewModel lié à l'Activity parente
viewModel = new ViewModelProvider(requireActivity()).get(CounterViewModel.class);

// Observer lifecycle-aware → zéro crash, zéro memory leak
viewModel.getCount().observe(getViewLifecycleOwner(), newCount -> {
    tvCount2.setText(String.valueOf(newCount));
});
```

---

## 9. Tests approfondis

### Vidéo 1 — Sans ViewModel et sans `onSaveInstanceState`

Dans ce test, `onSaveInstanceState()` est **commenté**. Le compteur est incrémenté plusieurs fois, puis l'écran est tourné. Le compteur **revient à 0** car la variable d'instance est perdue à la destruction de l'Activity — aucune sauvegarde n'est effectuée. C'est le problème classique que tout développeur Android rencontrait avant Jetpack.

> **Résultat observé :** Compteur remis à zéro après rotation.

<!-- Remplace ce commentaire par ta vidéo GitHub une fois uploadée -->
> 🎥 **Démonstration :**
>
> 
https://github.com/user-attachments/assets/cf62a9c0-3c0a-4d97-8e25-c532fba3be50

---

### Vidéo 2 — Comparaison : `onSaveInstanceState` vs ViewModel + LiveData

Cette vidéo regroupe les deux derniers tests pour mettre en évidence le fait que le compteur **ne se réinitialise pas**, quelle que soit l'approche utilisée — mais pour des raisons fondamentalement différentes.

**Test 2a — Sans ViewModel mais avec `onSaveInstanceState` :**

`onSaveInstanceState()` est **actif**. Le compteur est incrémenté, puis l'écran est tourné. La valeur est sauvegardée dans le `Bundle` et restaurée dans `onCreate` — le compteur **survit à la rotation**. Cependant cette approche reste une solution de contournement : elle ne fonctionne qu'avec des types primitifs, pas avec des objets complexes, des threads ou des LiveData.

> **Résultat observé :** Compteur conservé après rotation grâce au Bundle (primitifs uniquement).

**Test 2b — Avec ViewModel + LiveData :**

Le compteur est géré par `CounterViewModel`. L'écran est tourné plusieurs fois — la valeur est **toujours conservée** sans aucun `onSaveInstanceState`. Le `ViewModelStore` maintient l'instance du ViewModel en vie tant que l'Activity n'est pas définitivement détruite. Le bouton **INCREMENT (THREAD)** déclenche un `postValue()` depuis un thread background — la mise à jour de l'UI se fait proprement sur le thread principal, sans risque de crash.

> **Résultat observé :** Compteur intact après rotations multiples, UI mise à jour automatiquement via LiveData.

<!-- Remplace ce commentaire par ta vidéo GitHub une fois uploadée -->
> 🎥 **Démonstration (Tests 2a et 2b) :**
> 

https://github.com/user-attachments/assets/341390bc-e237-44a7-8d7b-856be18a1f5f

---

## 10. Tableau comparatif

| Critère | Sans ViewModel (Test 1) | Sans ViewModel + onSaveInstance (Test 2) | Avec ViewModel + LiveData (Test 3) |
|---|---|---|---|
| Survie à la rotation | ❌ Non | ⚠️ Partielle (Bundle) | ✅ Oui (ViewModelStore) |
| Types de données supportés | Primitifs uniquement | Primitifs uniquement | Tout objet complexe |
| Mise à jour UI automatique | ❌ Manuelle | ❌ Manuelle | ✅ Observer LiveData |
| Thread background safe | ❌ Non | ❌ Non | ✅ `postValue()` |
| Lifecycle-aware | ❌ Non | ❌ Non | ✅ Oui |
| Memory leak possible | ✅ Non (simple) | ✅ Non (simple) | ✅ Non (observer auto-supprimé) |
| Code propre (MVVM) | ❌ Mélangé | ❌ Mélangé | ✅ Séparé |

---

## 12. Conclusion

Ce laboratoire m'a permis de comprendre et maîtriser le cœur de l'architecture Android moderne. La comparaison directe entre les trois approches dans une seule application rend le contraste immédiatement visible :

- **Sans ViewModel** : perte de données à chaque rotation, code mélangé, fragile
- **Avec `onSaveInstanceState`** : solution partielle, limitée aux types primitifs
- **Avec ViewModel + LiveData** : données persistantes, UI réactive, code propre, zéro memory leak, conforme aux standards Google 2026

C'est exactement le pattern **MVVM** qu'utilisent toutes les apps professionnelles Android. Ce lab constitue la base pour tout projet Jetpack (Room, Navigation, DataBinding, etc.).

---

## 👤 Auteur

**DOSSAH Landry**  
ENSA Marrakech | GCDSTE S4  
Module : Programmation Mobile Android — Java
