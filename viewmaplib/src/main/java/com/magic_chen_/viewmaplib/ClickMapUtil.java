package com.magic_chen_.viewmaplib;

import android.app.Activity;
import android.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;


/**
 * Created by magic_chen_ on 2019/10/18.
 */
public class ClickMapUtil {
    static final FindViewAbility<Activity> ACTIVITY_FIND_VIEW = new FindViewAbility<Activity>() {
        @Override
        public View findViewById(final Activity activity, final int id) {
            return activity.findViewById(id);
        }
    };

    static final FindViewAbility<View> ROOTVIEW_FIND_VIEW = new FindViewAbility<View>() {
        @Override
        public View findViewById(final View view, final int id) {
            return view.findViewById(id);
        }
    };

    private static final Class[] HANDLER_INTERFACE_CLASS = new Class[]{View.OnClickListener.class};

    public static <Type> void map(Object object, Type type, FindViewAbility<Type> findViewAbility) {
        assert object != null;
        Class<?> clazz = object.getClass();
        final ClickHandler clickHandler = new ClickHandler(object);
        final HashMap<Integer, Method> idMethodMap = clickHandler.kIdMethodMap;
        final View.OnClickListener onCLs = (View.OnClickListener) Proxy.newProxyInstance(clazz.getClassLoader(),
                HANDLER_INTERFACE_CLASS, clickHandler);
        while (clazz != null && !Activity.class.equals(clazz) && !Fragment.class.equals(clazz) && !View.class.equals
                (clazz) && !ViewGroup.class.equals(clazz)) {
            final Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                final ClickMapping click = method.getAnnotation(ClickMapping.class);
                if (click == null) {
                    continue;
                }

                for (int i = 0; i < click.value().length; ++i) {
                    final int viewId = click.value()[i];
                    final View view = findViewAbility.findViewById(type, viewId);
                    if (view == null) {
                        continue;
                    }
                    if (idMethodMap.containsKey(viewId)) {
                        continue;
                    }
                    idMethodMap.put(viewId, method);
                    view.setOnClickListener(onCLs);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    public static void map(Activity activity) {
        map(activity, activity, ACTIVITY_FIND_VIEW);
    }

    public static void map(View view, Object object) {
        map(object, view, ROOTVIEW_FIND_VIEW);
    }

    /**
     * 获取View的接口，假如不用Activity或者根View，就需要实现这个接口了
     *
     * @param <Type>
     */
    public static interface FindViewAbility<Type> {
        View findViewById(Type t, int id);
    }

    static class ClickHandler<Type> implements InvocationHandler {
        final HashMap<Integer, Method> kIdMethodMap = new HashMap<Integer, Method>();

        Type kType;

        ClickHandler(final Type type) {
            kType = type;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            View view = (View) args[0];
            int viewId = view.getId();
            if (!kIdMethodMap.containsKey(view.getId())) {
                return null;
            }
            Method $method = kIdMethodMap.get(viewId);
            $method.setAccessible(true);
            if ($method.getParameterTypes().length == 1)
                $method.invoke(kType, args);
            else
                $method.invoke(kType);
            return null;
        }
    }
}
