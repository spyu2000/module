// GPSEncodeDlg.h : header file
//

#if !defined(AFX_GPSENCODEDLG_H__A8E9F0A5_F1CA_47C5_AE95_2CB8B641D204__INCLUDED_)
#define AFX_GPSENCODEDLG_H__A8E9F0A5_F1CA_47C5_AE95_2CB8B641D204__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

/////////////////////////////////////////////////////////////////////////////
// CGPSEncodeDlg dialog

class CGPSEncodeDlg : public CDialog
{
// Construction
public:
	CGPSEncodeDlg(CWnd* pParent = NULL);	// standard constructor

// Dialog Data
	//{{AFX_DATA(CGPSEncodeDlg)
	enum { IDD = IDD_GPSENCODE_DIALOG };
	double	m_oldLo;
	double	m_oldLa;
	double	m_newLo;
	double	m_newLa;
	CString	m_StrOpenPath;
	CString	m_StrSavePath;
	//}}AFX_DATA

	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CGPSEncodeDlg)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:
	HICON m_hIcon;

	// Generated message map functions
	//{{AFX_MSG(CGPSEncodeDlg)
	virtual BOOL OnInitDialog();
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	afx_msg void OnButton1();
	afx_msg void OnButton2();
	afx_msg void OnClose();
	afx_msg void OnButtonOpen();
	afx_msg void OnButtonSave();
	afx_msg void OnButtonCalcall();
	afx_msg void OnButton3();
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
		
private:
	HINSTANCE hDll;
};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_GPSENCODEDLG_H__A8E9F0A5_F1CA_47C5_AE95_2CB8B641D204__INCLUDED_)
