package com.magic_chen_.viewmaplib;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * Created by magic_chen_ on 2019/10/18.
 */
public final class ViewMapUtil {

    /**
     * @param object   要映射对象
     * @param rootView 要映射对象所要查询的根控件
     */
    public static void map(Object object, View rootView) {
        Class<?> clazz = object.getClass();

        while (clazz != null &&
                clazz != Activity.class &&
                clazz != View.class) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields) {
                ViewMapping mapping = f.getAnnotation(ViewMapping.class);
                int id = 0;
                if (mapping == null) {
                    continue;
                }
                try {
                    id = mapping.value();
                    f.setAccessible(true);
                    View childView = rootView.findViewById(id);
                    if (childView == null) {
                        continue;
                    }
                    f.set(object, childView);
                } catch (Exception e) {
                    System.err.println(String.format(
                            "view map error = %h, clazz:%s, field:%s", id,
                            clazz.getSimpleName(), f.getName()));
                    e.printStackTrace();
                    throw new RuntimeException();
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    public static View map(Object object) {
        final LayoutInflater inflater;
        if (object instanceof Activity) {
            inflater = ((Activity) object).getLayoutInflater();
        } else {
            if (ViewMapService.getInstance().mApp == null) {
                inflater = null;
                try {
                    throw new Exception("plz init ViewMapService");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                inflater = (LayoutInflater) ViewMapService.getInstance().mApp.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
        }

        Log.d("ViewMapUtil","   ===>  object:"+object+"  inflater:"+inflater);
        return map(object, inflater, null);
    }


    public static <T> T mapForRecyclerView(
            Context context, Class<T> clazz, ViewGroup parentView) {
        T object = null;
        try {
            View rootView = LayoutInflater.from(context).inflate(
                    getViewMapping(clazz).value(), parentView, false);
            Constructor<T> constructor = clazz.getConstructor(View.class);
            constructor.setAccessible(true);
            object = constructor.newInstance(rootView);
            map(object, rootView);
            rootView.setTag(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return object;
    }

    public static View map(Object object, int layoutId) {
        final LayoutInflater inflater;
        if (object instanceof Activity) {
            inflater = ((Activity) object).getLayoutInflater();
        } else {
            if (ViewMapService.getInstance().mApp == null) {
                inflater = null;
                try {
                    throw new Exception("plz init ViewMapService");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                inflater = (LayoutInflater) ViewMapService.getInstance().mApp.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
        }
        final View root = inflater.inflate(layoutId, null, false);
        map(object, root);
        return root;
    }

    public static View map(Object object, int layoutId, LayoutInflater inflater) {
        final View root = inflater.inflate(layoutId, null);
        map(object, root);
        return root;
    }

    public static View map(Object object, LayoutInflater inflater,
                           ViewGroup root) {
        View rootView = inflater.inflate(getViewMapping(object.getClass()).value(), root, false);
        map(object, rootView);
        return rootView;
    }

    public static View mapForMerge(Object object, int layoutId,
                                   ViewGroup view) {
        View rootView = null;
        if (ViewMapService.getInstance() == null) {
            try {
                throw new Exception("plz init ViewMapService");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            rootView = ((LayoutInflater) ViewMapService.getInstance().mApp.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                    layoutId, view);;
        }
        map(object, rootView);
        return rootView;
    }

    /**
     * 根据ViewHolder的Class对象，新建一个ViewHolder类和对应Layout的View对象
     *
     * @return Pair.first是对应的ViewHolder，Pair.second是ViewHolder注解里面的Layout对应的View
     */
    public static <T> Pair<T, View> map(Class<T> clazz,
                                        LayoutInflater inflater, ViewGroup root) {
        Pair<T, View> pair = null;
        T object;
        try {
            object = (T) clazz.getConstructor(new Class[]{}).newInstance();
            View rootView = inflater.inflate(getViewMapping(clazz).value(),
                    root, false);
            pair = new Pair<T, View>(object, rootView);
            map(object, rootView);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pair;
    }

    public static <T> Pair<T, View> mapForConvert(Class<T> clazz,
                                                  View convertView,
                                                  ViewGroup parentView) {
        final T $t;
        if (convertView != null) {
            $t = (T) convertView.getTag();
        } else {

            if (ViewMapService.getInstance().mApp == null) {
                $t = null;
                try {
                    throw new Exception("plz init ViewMapService");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Pair<T, View> pair = map(clazz,
                        (LayoutInflater) ViewMapService.getInstance().mApp.getSystemService(Context.LAYOUT_INFLATER_SERVICE), parentView);
                $t = pair.first;
                convertView = pair.second;
                convertView.setTag($t);

            }

        }
        return new Pair<T, View>($t, convertView);
    }

    static ViewMapping getViewMapping(Class<?> clazz) {
        ViewMapping mapping;

        while ((!Activity.class.equals(clazz))
                && clazz != null) {
            mapping = clazz.getAnnotation(ViewMapping.class);
            if (mapping != null) {
                return mapping;
            } else {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}
