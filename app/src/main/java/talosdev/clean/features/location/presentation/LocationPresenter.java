package talosdev.clean.features.location.presentation;

import android.util.Log;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import talosdev.clean.common.PermissionRequestHandler;
import talosdev.clean.features.location.domain.LocationInteractor;
import talosdev.clean.features.location.domain.model.NoLocationAvailableException;

public class LocationPresenter implements LocationContract.Presenter {

    private static final String TAG = "LOCATION";

    private final WeakReference<LocationContract.View> viewWeakReference;
    private final LocationInteractor interactor;
    private PermissionRequestHandler permissionRequestHandler;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Inject
    public LocationPresenter(LocationContract.View view,
                             LocationInteractor interactor,
                             PermissionRequestHandler permissionRequestHandler) {
        this.viewWeakReference = new WeakReference<>(view);
        this.interactor = interactor;
        this.permissionRequestHandler = permissionRequestHandler;
    }

    @Override
    public void init() {
        disposables.add(
                permissionRequestHandler.getResultStream()
                .subscribe(
                        this::handleResult,
                        throwable -> Log.e(TAG, "An error occurred on the permission request " +
                                "result stream", throwable)
                )
        );
    }

    private void handleResult(Boolean granted) {
        LocationContract.View view = viewWeakReference.get();
        if (view != null) {
            if (granted) {
                getLocation();
            } else {
                view.showNeedPermission();
            }
        }
    }

    @Override
    public void getLocation() {
        disposables.add(
                interactor.getLocation()
                        .subscribe(
                                location -> {
                                    LocationContract.View view = viewWeakReference.get();
                                    if (view != null) {
                                        view.showLatitude(String.valueOf(location.latitude()));
                                        view.showLongitude(String.valueOf(location.longitude()));
                                    }
                                },
                                throwable -> {
                                    LocationContract.View view = viewWeakReference.get();
                                    if (view != null) {
                                        Log.e(TAG,"Error while getting location", throwable);
                                        if (throwable instanceof NoLocationAvailableException) {
                                            view.showNoLocationAvailable();
                                        } else {
                                            view.showGenericError();
                                        }

                                    }
                                }

                        )
        );
    }


    @Override
    public void cleanup() {
        disposables.clear();
    }
}
