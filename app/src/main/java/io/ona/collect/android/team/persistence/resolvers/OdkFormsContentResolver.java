package io.ona.collect.android.team.persistence.resolvers;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import io.ona.collect.android.team.BuildConfig;
import io.ona.collect.android.team.application.TeamManagement;
import io.ona.collect.android.team.persistence.carriers.OdkForm;

/**
 * Created by Jason Rogena - jrogena@ona.io on 07/08/2017.
 */

public class OdkFormsContentResolver {
    private static final String AUTHORITY = BuildConfig.ODK_COLLECT_PACKAGE_NAME + ".provider.odk.forms";
    private static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/forms");
    private static final String COLUMN_DISPLAY_NAME = "displayName";
    private static final String COLUMN_DESCRIPTION = "description";  // can be null
    private static final String COLUMN_JR_FORM_ID = "jrFormId";
    private static final String COLUMN_JR_VERSION = "jrVersion"; // can be null
    private static final String COLUMN_JR_DOWNLOAD_URL = "jrDownloadUrl"; // can be null
    private static final String COLUMN_JR_MANIFEST_URL = "jrManifestUrl"; // can be null
    private static final String COLUMN_FORM_FILE_PATH = "formFilePath";
    private static final String COLUMN_SUBMISSION_URI = "submissionUri"; // can be null
    private static final String COLUMN_BASE64_RSA_PUBLIC_KEY = "base64RsaPublicKey"; // can be null
    private static final String COLUMN_DISPLAY_SUBTEXT = "displaySubtext";
    private static final String COLUMN_MD5_HASH = "md5Hash";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_JRCACHE_FILE_PATH = "jrcacheFilePath";
    private static final String COLUMN_FORM_MEDIA_PATH = "formMediaPath";
    private static final String COLUMN_LANGUAGE = "language";

    /**
     * Returns a list of all ODK forms downloaded on ODK collect
     *
     * @return
     */
    public List<OdkForm> getAllDownloadedForms() throws OdkForm.MalformedObjectException {
        List<OdkForm> forms = new ArrayList<>();
        ContentResolver cr = TeamManagement.getInstance().getContentResolver();
        Cursor cursor = null;
        try {
            cursor = cr.query(CONTENT_URI, null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                    OdkForm curForm = extractOdkForm(cursor);
                    if (curForm != null) {
                        forms.add(curForm);
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return forms;
    }

    private OdkForm extractOdkForm(Cursor cursor) throws OdkForm.MalformedObjectException {
        if (cursor != null && !cursor.isAfterLast()) {
            OdkForm form = new OdkForm(
                    cursor.getString(cursor.getColumnIndex(COLUMN_JR_FORM_ID)),
                    OdkForm.STATE_FORM_DOWNLOADED);
            form.setDisplayName(cursor.getString(cursor.getColumnIndex(COLUMN_DISPLAY_NAME)));
            form.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
            form.setJrVersion(cursor.getString(cursor.getColumnIndex(COLUMN_JR_VERSION)));
            form.setJrDownloadUrl(cursor.getString(cursor.getColumnIndex(COLUMN_JR_DOWNLOAD_URL)));
            form.setJrManifestUrl(cursor.getString(cursor.getColumnIndex(COLUMN_JR_MANIFEST_URL)));
            form.setFormFilePath(cursor.getString(cursor.getColumnIndex(COLUMN_FORM_FILE_PATH)));
            form.setSubmissionUri(cursor.getString(cursor.getColumnIndex(COLUMN_SUBMISSION_URI)));
            form.setBase64RSAPublicKey(cursor.getString(cursor.getColumnIndex(COLUMN_BASE64_RSA_PUBLIC_KEY)));
            form.setDisplaySubtext(cursor.getString(cursor.getColumnIndex(COLUMN_DISPLAY_SUBTEXT)));
            form.setMd5Hash(cursor.getString(cursor.getColumnIndex(COLUMN_MD5_HASH)));
            form.setDate(cursor.getLong(cursor.getColumnIndex(COLUMN_DATE)));
            form.setJrCacheFilePath(cursor.getString(cursor.getColumnIndex(COLUMN_JRCACHE_FILE_PATH)));
            form.setFormMediaPath(cursor.getString(cursor.getColumnIndex(COLUMN_FORM_MEDIA_PATH)));
            form.setLanguage(cursor.getString(cursor.getColumnIndex(COLUMN_LANGUAGE)));

            return form;
        }

        return null;
    }
}
