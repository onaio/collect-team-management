package io.ona.collect.android.team.persistence.carriers;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Jason Rogena - jrogena@ona.io on 07/08/2017.
 */

public class OdkForm {
    private static final Pattern ONA_DATA_DOWNLOAD_URL_PATTERN =
            Pattern.compile("http[s]?://[a-zA-Z0-9\\.\\-]+/\\w+/forms/(\\d+)/form.xml");
    private static final ArrayList<Pattern> SUPPORTED_DOWNLOAD_PATTERNS;
    public static final String STATE_FORM_DOWNLOADED = "form_downloaded";
    public static final String STATE_FORM_DELETED = "form_deleted";
    public static final String STATE_SUBMISSION_CREATED = "submission_created";
    public static final String STATE_SUBMISSION_EDITED = "submission_edited";
    public static final String STATE_SUBMISSIONS_SENT = "submissions_sent";
    public static final String STATE_SUBMISSIONS_DELETED = "submissions_deleted";
    public static final String KEY_STATE = "state";
    public static final String KEY_FORM_NAME = "form_name";
    public static final String KEY_DOWNLOAD_URL = "download_url";
    public static final String KEY_MANIFEST_URL = "manifest_url";
    public static final String KEY_FORM_ID = "form_id";
    public static final String KEY_FORM_VERSION = "form_version";

    static {
        SUPPORTED_DOWNLOAD_PATTERNS = new ArrayList<>();
        SUPPORTED_DOWNLOAD_PATTERNS.add(ONA_DATA_DOWNLOAD_URL_PATTERN);
    }

    public final String state;
    public final String jrFormId;
    private String displayName;
    private String description;
    private String jrVersion;
    private String jrDownloadUrl;
    private String jrManifestUrl;
    private String formFilePath;
    private String submissionUri;
    private String base64RSAPublicKey;
    private String displaySubtext;
    private String md5Hash;
    private Long date;
    private String jrCacheFilePath;
    private String formMediaPath;
    private String language;


    public OdkForm(String jrFormId, String state) throws MalformedObjectException {
        this.state = state;
        this.jrFormId = jrFormId;
        if (state == null ||
                (!state.equals(STATE_FORM_DOWNLOADED)
                        && !state.equals(STATE_FORM_DELETED)
                        && !state.equals(STATE_SUBMISSION_CREATED)
                        && !state.equals(STATE_SUBMISSION_EDITED)
                        && !state.equals(STATE_SUBMISSIONS_SENT)
                        && !state.equals(STATE_SUBMISSIONS_DELETED))) {
            throw new MalformedObjectException("OdkForm has unknown state");
        }
        if (TextUtils.isEmpty(jrFormId)) {
            throw new MalformedObjectException("OdkForm has an empty formId");
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getJrVersion() {
        return jrVersion;
    }

    public void setJrVersion(String jrVersion) {
        this.jrVersion = jrVersion;
    }

    public String getJrDownloadUrl() {
        return jrDownloadUrl;
    }

    public void setJrDownloadUrl(String jrDownloadUrl) {
        this.jrDownloadUrl = jrDownloadUrl;
    }

    public String getJrManifestUrl() {
        return jrManifestUrl;
    }

    public void setJrManifestUrl(String jrManifestUrl) {
        this.jrManifestUrl = jrManifestUrl;
    }

    public String getFormFilePath() {
        return formFilePath;
    }

    public void setFormFilePath(String formFilePath) {
        this.formFilePath = formFilePath;
    }

    public String getSubmissionUri() {
        return submissionUri;
    }

    public void setSubmissionUri(String submissionUri) {
        this.submissionUri = submissionUri;
    }

    public String getBase64RSAPublicKey() {
        return base64RSAPublicKey;
    }

    public void setBase64RSAPublicKey(String base64RSAPublicKey) {
        this.base64RSAPublicKey = base64RSAPublicKey;
    }

    public String getDisplaySubtext() {
        return displaySubtext;
    }

    public void setDisplaySubtext(String displaySubtext) {
        this.displaySubtext = displaySubtext;
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    public void setMd5Hash(String md5Hash) {
        this.md5Hash = md5Hash;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getJrCacheFilePath() {
        return jrCacheFilePath;
    }

    public void setJrCacheFilePath(String jrCacheFilePath) {
        this.jrCacheFilePath = jrCacheFilePath;
    }

    public String getFormMediaPath() {
        return formMediaPath;
    }

    public void setFormMediaPath(String formMediaPath) {
        this.formMediaPath = formMediaPath;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Attempts to extract a form's primary key from its download url. Returns the form's Java Rosa
     * formId if unable to get a primary key
     *
     * @return
     */
    public String getPkid() {
        if (!TextUtils.isEmpty(jrDownloadUrl)) {
            for (Pattern curSupportedPattern : SUPPORTED_DOWNLOAD_PATTERNS) {
                Matcher matcher = curSupportedPattern.matcher(jrDownloadUrl);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }

        return jrFormId;
    }

    public static OdkForm getFromBundle(Bundle bundle) throws MalformedObjectException {
        OdkForm form = new OdkForm(bundle.getString(KEY_FORM_ID), bundle.getString(KEY_STATE));
        form.setDisplayName(bundle.getString(KEY_FORM_NAME));
        form.setJrDownloadUrl(bundle.getString(KEY_DOWNLOAD_URL));
        form.setJrManifestUrl(bundle.getString(KEY_MANIFEST_URL));
        form.setJrVersion(bundle.getString(KEY_FORM_VERSION));
        return form;
    }

    public static class MalformedObjectException extends Exception {
        public MalformedObjectException() {
            super();
        }

        public MalformedObjectException(String message) {
            super(message);
        }

        public MalformedObjectException(String message, Throwable cause) {
            super(message, cause);
        }

        public MalformedObjectException(Throwable cause) {
            super(cause);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        protected MalformedObjectException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }
}
