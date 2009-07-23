/*******************************************************************************
* Copyright (c) 2009 Thales Corporate Services SAS                             *
* Author : Gregory Boissinot                                                   *
*                                                                              *
* Permission is hereby granted, free of charge, to any person obtaining a copy *
* of this software and associated documentation files (the "Software"), to deal*
* in the Software without restriction, including without limitation the rights *
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
* copies of the Software, and to permit persons to whom the Software is        *
* furnished to do so, subject to the following conditions:                     *
*                                                                              *
* The above copyright notice and this permission notice shall be included in   *
* all copies or substantial portions of the Software.                          *
*                                                                              *
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
* THE SOFTWARE.                                                                *
*******************************************************************************/

package com.thalesgroup.hudson.plugins.cppcheck;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.thalesgroup.hudson.plugins.cppcheck.model.CppcheckFile;

public class CppcheckReport implements ICheckstyleReport, Serializable {

	private static final long serialVersionUID = 1L;

	private Map<Integer, CppcheckFile> internalMap;
	
    private List<CppcheckFile> everyErrors;

	private List<CppcheckFile> allErrors;
    
    private List<CppcheckFile> styleErrors;

    private List<CppcheckFile> allStyleErrors;

    private List<CppcheckFile> errorErrors;
    
    private List<CppcheckFile> noCategoryErrors;

    public List<CppcheckFile> getEveryErrors() {
        return everyErrors;
    }

    public List<CppcheckFile> getAllErrors() {
        return allErrors;
    }

    public List<CppcheckFile> getStyleErrors() {
        return styleErrors;
    }

    public List<CppcheckFile> getAllStyleErrors() {
        return allStyleErrors;
    }

    public List<CppcheckFile> getErrorErrors() {
        return errorErrors;
    }

    public void setEveryErrors(List<CppcheckFile> everyErrors) {
        this.everyErrors = everyErrors;
    }

    public void setAllErrors(List<CppcheckFile> allErrors) {
        this.allErrors = allErrors;
    }

    public void setStyleErrors(List<CppcheckFile> styleErrors) {
        this.styleErrors = styleErrors;
    }

    public void setAllStyleErrors(List<CppcheckFile> allStyleErrors) {
        this.allStyleErrors = allStyleErrors;
    }

    public void setErrorErrors(List<CppcheckFile> errorErrors) {
        this.errorErrors = errorErrors;
    }

	public Map<Integer, CppcheckFile> getInternalMap() {
		return internalMap;
	}

	public void setInternalMap(Map<Integer, CppcheckFile> internalMap) {
		this.internalMap = internalMap;
	}

	public List<CppcheckFile> getNoCategoryErrors() {
		return noCategoryErrors;
	}

	public void setNoCategoryErrors(List<CppcheckFile> noCategoryErrors) {
		this.noCategoryErrors = noCategoryErrors;
	}
	
	public int getNumberErrors(){
		return (everyErrors==null)?0:everyErrors.size();
	}
}
