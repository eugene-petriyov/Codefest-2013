package ru.codefest.client.android.dao;

import java.util.ArrayList;
import java.util.List;

import ru.codefest.client.android.model.Category;
import ru.codefest.client.android.model.CodeFestItem;
import ru.codefest.client.android.model.Lecture;
import ru.codefest.client.android.model.LecturePeriod;
import ru.codefest.client.android.provider.CodeFestProvider;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.SparseIntArray;

import com.petriyov.android.libs.bindings.BinderHelper;
import com.petriyov.android.libs.contentprovider.CustomContentProvider;

public class CodeFestDao {

    private Context context;

    private BinderHelper binderHelper;

    public CodeFestDao(Context context, BinderHelper binderHelper) {
        this.context = context;
        this.binderHelper = binderHelper;
    }

    public void batchFavorite(SparseIntArray favoritesArray) {
        ArrayList<ContentProviderOperation> updateOperations = new ArrayList<ContentProviderOperation>();
        for (int i = 0; i < favoritesArray.size(); i++) {
            String lectureId = String.valueOf(favoritesArray.keyAt(i));
            int isFavorite = favoritesArray.get(favoritesArray.keyAt(i));
            updateOperations.add(ContentProviderOperation
                    .newUpdate(CodeFestProvider.getUri(Lecture.TABLE_NAME))
                    .withSelection(CustomContentProvider.KEY_ID + " IN (?)",
                            new String[]{lectureId})
                    .withValue(Lecture.IS_FAVORITE, isFavorite).build());
        }
        try {
            context.getContentResolver().applyBatch(
                    CodeFestProvider.CONTENT_URI, updateOperations);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
            new RuntimeException(e);
        }
    }

    public <T extends CodeFestItem> void bulkInsertItems(List<T> list,
                                                         String tableName) {
        context.getContentResolver().bulkInsert(
                CodeFestProvider.getUri(tableName),
                binderHelper.adaptValuesFromList(list));
    }

    public void deleteAllEntities() {
        ArrayList<ContentProviderOperation> deleteOperations = new ArrayList<ContentProviderOperation>();
        deleteOperations.add(ContentProviderOperation.newDelete(
                CodeFestProvider.getUri(Category.TABLE_NAME)).build());
        deleteOperations.add(ContentProviderOperation.newDelete(
                CodeFestProvider.getUri(LecturePeriod.TABLE_NAME)).build());
        deleteOperations.add(ContentProviderOperation.newDelete(
                CodeFestProvider.getUri(Lecture.TABLE_NAME)).build());
        try {
            context.getContentResolver().applyBatch(
                    CodeFestProvider.CONTENT_URI, deleteOperations);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
            new RuntimeException(e);
        }
    }

    public Category getCategoryById(int categoryId) {
        Cursor cursor = context.getContentResolver().query(
                CodeFestProvider.getUri(Category.TABLE_NAME), null,
                CustomContentProvider.KEY_ID + " = ?1",
                new String[]{String.valueOf(categoryId)}, null);
        return binderHelper.adaptFromCursor(cursor, Category.class);
    }

    public List<Lecture> getFavoritesByPeriodId(int periodId) {
        List<Lecture> lectureList = new ArrayList<Lecture>();
        Cursor cursor = context.getContentResolver()
                .query(CodeFestProvider.getUri(Lecture.TABLE_NAME),
                        null,
                        Lecture.PERIOD_ID + " = ?1 AND " + Lecture.IS_FAVORITE
                                + " = ?2",
                        new String[]{String.valueOf(periodId),
                                String.valueOf(Lecture.FAVORITE)},
                        Lecture.CATEGORY_ID);
        lectureList = binderHelper.adaptListFromCursor(cursor, Lecture.class);
        return lectureList;

    }

    public Lecture getLectureById(int lectureId) {
        Cursor cursor = context.getContentResolver().query(
                CodeFestProvider.getUri(Lecture.TABLE_NAME), null,
                CustomContentProvider.KEY_ID + " = ?1",
                new String[]{String.valueOf(lectureId)}, null);
        return binderHelper.adaptFromCursor(cursor, Lecture.class);
    }

    public List<Lecture> getLecturesByPeriodId(int periodId) {
        List<Lecture> lectureList = new ArrayList<Lecture>();
        Cursor cursor = context.getContentResolver().query(
                CodeFestProvider.getUri(Lecture.TABLE_NAME), null,
                Lecture.PERIOD_ID + " = ?1",
                new String[]{String.valueOf(periodId)}, Lecture.CATEGORY_ID);
        lectureList = binderHelper.adaptListFromCursor(cursor, Lecture.class);
        return lectureList;

    }

    public <T extends CodeFestItem> List<T> getList(Class<T> clazz,
                                                    String tableName) {
        List<T> list = new ArrayList<T>();
        Cursor cursor = context.getContentResolver().query(
                CodeFestProvider.getUri(tableName), null, null, null, null);
        list = binderHelper.adaptListFromCursor(cursor, clazz);
        return list;
    }

    public List<LecturePeriod> getNotEmptyPeriods() {
        List<LecturePeriod> list = new ArrayList<LecturePeriod>();
        Cursor cursor = context.getContentResolver().query(
                CodeFestProvider.getUri(LecturePeriod.TABLE_NAME),
                null,
                "exists (select " + CustomContentProvider.KEY_ID
                        + " from lecture where " + Lecture.PERIOD_ID
                        + " = lecturePeriod." + CustomContentProvider.KEY_ID
                        + ")", null, null);
        list = binderHelper.adaptListFromCursor(cursor, LecturePeriod.class);
        return list;

    }
}
